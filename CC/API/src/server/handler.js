const dbase = require('./database')
const { nanoid } = require('nanoid')
const jwt = require('jsonwebtoken')
const nodemailer = require('nodemailer')
const crypto = require('crypto')
const axios = require('axios')
const moment = require('moment-timezone')
const FormData = require('form-data')
const { Storage } = require('@google-cloud/storage')
const JWT_SECRET = process.env.JWT_SECRET

const streamToBuffer = async(stream) => {
    return new Promise((resolve, reject) => {
        const chunks = []
        stream.on('data', (chunk) => chunks.push(chunk))
        stream.on('end', () => resolve(Buffer.concat(chunks)))
        stream.on('error', reject)
    })
}

const storage = new Storage({
    projectId: process.env.PROJECTID,
    keyFilename: process.env.KEYSERVICEACCOUNT
}) 

const bucketName = process.env.BUCKETNAME
const bucket = storage.bucket(bucketName) 

const GenerateToken = (user) => {
    const token = jwt.sign({id: user.id, email: user.email}, JWT_SECRET, {expiresIn: '1h'})
    return token
}

const blacklistToken = async (token) => {
    await dbase.query('INSERT INTO blacklisted_tokens (token) VALUES (?)', [token])
}

const isTokenBlacklisted = async (token) => {
    const result = await dbase.query('SELECT COUNT(*) AS count FROM blacklisted_tokens WHERE token = ?', [token])
    return result[0][0].count > 0
}

const SaveImagetoGCS = async(imageBuffer, filename) => {
    const file = bucket.file(filename)
    await file.save(imageBuffer, {
        contentType: 'image/jpeg',
        public: true,
    })
    return `https://storage.googleapis.com/${bucketName}/${filename}`
} 

const AccessValidation = async (request, h) => {
    const authorization = request.headers.authorization

    if (!authorization) {
        const response = h.response({
            status: 'fail',
            message: 'Token tidak ditemukan'
        })
        response.code(401)
        return response.takeover()
    }

    const token = authorization.split(' ')[1]

    try {
        // Periksa apakah token ada dalam daftar blacklist
        const isBlacklisted = await isTokenBlacklisted(token)
        if (isBlacklisted) {
            const response = h.response({
                status: 'fail',
                message: 'Token telah di-blacklist'
            })
            response.code(401)
            return response.takeover()
        }

        // Verifikasi token
        const jwtDecode = jwt.verify(token, JWT_SECRET)
        request.auth = { credentials: jwtDecode }
        return h.continue

    } catch (error) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response.takeover()
    }
}

const SignUp = async (request, h) => {
    const { username, email, phonenumber, password } = request.payload
    try {
        // Cek apakah email sudah digunakan
        const [existingUsers] = await dbase.query('SELECT id FROM users WHERE email = ?', [email])

        if (existingUsers.length > 0) {
            const response = h.response({
                status: 'fail',
                message: 'Gagal untuk menambahkan, email sudah digunakan'
            })
            response.code(400)
            return response
        }

        // Tambahkan pengguna baru
        const id = nanoid(16)
        const [result] = await dbase.query('INSERT INTO users (id, username, email, phonenumber, password) VALUES (?, ?, ?, ?, ?)', [id, username, email, phonenumber, password])

        if (result.affectedRows === 1) {
            const response = h.response({
                status: 'success',
                message: 'User berhasil ditambahkan',
            })
            response.code(201)
            return response
        } else {
            throw new Error('User gagal ditambahkan');
        }
    } catch (error) {
        console.error('Error', error)
        const response = h.response({
            status: 'fail',
            message: 'User gagal ditambahkan'
        })
        response.code(500)
        return response
    }
}

const SignIn = async (request, h) => {
    const { email, password } = request.payload
    try {
        const [users] = await dbase.query('SELECT * FROM users WHERE email = ? AND password = ?', [email, password])

        if (users.length === 1) {
            const user = users[0]
            const token = GenerateToken(user)

            const response = h.response({
                status: 'success',
                message: 'Berhasil login ke user',
                data: {
                    user: {
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        phonenumber: user.phonenumber
                    }
                },
                token: token
            })
            response.code(200)
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'Invalid email or password'
            })
            response.code(404)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal login'
        })
        response.code(500)
        return response
    }
}


const ForgotPasswordSendEmail = async(request, h) => {
    const { email } = request.payload
    try {
        const [users] = await dbase.query('SELECT id FROM users WHERE email = ?', [email])

        if (users.length === 1) {
            const codeotp = nanoid(8)
            await dbase.query('INSERT INTO otp (email, codeotp) VALUES (?, ?)', [email, codeotp])

            const transporter = nodemailer.createTransport({
                service: 'gmail',
                auth: {
                    user: process.env.EMAIL_USER,
                    pass: process.env.EMAIL_PASS
                },
                tls: {
                    rejectUnauthorized: false
                }
            })

            await transporter.sendMail({
                from: 'U-Meal Application',
                to: email,
                subject: 'Kode OTP Verification',
                text: `This is your OTP code: ${codeotp}`
            })

            const response = h.response({
                status: 'success',
                message: 'Periksa email Anda untuk mereset kata sandi'
            })
            response.code(200)
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'Email tidak ditemukan'
            })
            response.code(404)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal mengirim email untuk reset kata sandi'
        })
        response.code(500)
        return response
    }
}

const ForgotPasswordChangePassword = async (request, h) => {
    const { codeotp, newPassword } = request.payload

    try {
        const [otps] = await dbase.query('SELECT email FROM otp WHERE codeotp = ?', [codeotp])

        if (otps.length === 1) {
            const email = otps[0].email
            await dbase.query('UPDATE users SET password = ? WHERE email = ?', [newPassword, email])
            await dbase.query('DELETE FROM otp WHERE codeotp = ?', [codeotp])

            const response = h.response({
                status: 'success',
                message: 'Kata sandi berhasil direset'
            })
            response.code(200);
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'OTP tidak valid'
            })
            response.code(400)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal mereset kata sandi'
        })
        response.code(500)
        return response
    }
}

const Logout = async (request, h) => {
    const authorization = request.headers.authorization

    if (!authorization) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response
    }

    const token = authorization.split(' ')[1]

    const [blacklistedTokens] = await dbase.query('SELECT token FROM blacklisted_tokens WHERE token = ?', [token])
    if (blacklistedTokens.length > 0) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response
    }

    try {
        jwt.verify(token, JWT_SECRET)
        await blacklistToken(token)

        const response = h.response({
            status: 'success',
            message: 'Logout berhasil'
        })
        response.code(200)
        return response
    } catch (error) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response
    }
}

const generateUniqueScanId = async () => {
    let uniqueIdFound = false;
    let newId
    while (!uniqueIdFound) {
        // Generate random 4 digit number within the range 1000-9999
        newId = Math.floor(1000 + Math.random() * 9000)
        // Check if the ID already exists in the database
        const [rows] = await dbase.query('SELECT id FROM scans WHERE id = ?', [newId])
        if (rows.length === 0) {
            uniqueIdFound = true
        }
    }
    return newId
}

const ScanImage = async (request, h) => {
    try {
        const { image, scanId } = request.payload

        if (scanId) {
            const [scan] = await dbase.query('SELECT id, user_id, total_kalori, scan_timestamp, image_url FROM scans WHERE id = ?', [scanId])
            const [scanDetails] = await dbase.query('SELECT jenis, kalori FROM scan_details WHERE scans_id = ?', [scanId])

            if (scan.length === 0) {
                const response = h.response({
                    status: 'fail',
                    message: 'Scan not found',
                })
                response.code(404)
                return response
            }

            const scanData = scan[0];
            const data = {
                ...scanData,
                detectedFoods: scanDetails
            }

            const response = h.response({
                status: 'success',
                message: 'Scan details fetched successfully',
                data,
            })
            response.code(200)
            return response
        }

        const imageBuffer = await streamToBuffer(image)

        // Prepare form data
        const form = new FormData();
        form.append('image', imageBuffer, {
            filename: 'upload.jpg',
            contentType: 'image/jpeg',
        })

        // Send POST request to Flask API
        const flaskApiResponse = await axios.post('http://127.0.0.1:8080/scan', form, {
            headers: form.getHeaders(),
        })

        // Process response from Flask API
        const detectedLabels = flaskApiResponse.data
        const userId = request.auth.credentials.id
        const scanTimestamp = new Date()
        const id_pict = nanoid(4)

        const gcsFilename = `${id_pict}.jpg`
        const imageUrl = await SaveImagetoGCS(imageBuffer, gcsFilename)

        let totalCalories = 0
        const foodEntries = []
        for (const foodName of detectedLabels) {
            const [foods] = await dbase.query('SELECT jenis, kalori FROM foods WHERE jenis = ?', [foodName])
            if (foods.length > 0) {
                const food = foods[0]
                totalCalories += food.kalori
                foodEntries.push({ jenis: food.jenis, kalori: food.kalori })
            }
        }

        const newScanId = await generateUniqueScanId();

        await dbase.query('INSERT INTO scans (id, user_id, total_kalori, scan_timestamp, image_url) VALUES (?, ?, ?, ?, ?)', [newScanId, userId, totalCalories, scanTimestamp, imageUrl])

        const scanDetailsValues = foodEntries.map(entry => [newScanId, userId, entry.jenis, entry.kalori])
        for (const values of scanDetailsValues) {
            await dbase.query('INSERT INTO scan_details (scans_id, user_id, jenis, kalori) VALUES (?, ?, ?, ?)', values)
        }

        const [daftarScan] = await dbase.query('SELECT jenis, kalori FROM scan_details WHERE scans_id = ?', [newScanId])

        const data = {
            imageUrl: imageUrl,
            scanId: newScanId,
            totalCalories,
            detectedFoods: daftarScan,
            scanTimestamp
        }

        const response = h.response({
            status: 'success',
            message: 'Model is predicted successfully',
            data,
        })
        response.code(201)
        return response

    } catch (error) {
        console.error('Error during scan:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal melakukan scan',
        })
        response.code(500)
        return response
    }
}

const GetHistory = async (request, h) => {
    try {
        const userId = request.params.id
        const { tanggal, bulan, tahun } = request.query

        let query = 'SELECT id, total_kalori, scan_timestamp, image_url FROM scans WHERE user_id = ?'
        const queryParams = [userId]

        if (tanggal) {
            query += ' AND DAY(scan_timestamp) = ?'
            queryParams.push(tanggal)
        }

        if (bulan) {
            query += ' AND MONTH(scan_timestamp) = ?'
            queryParams.push(bulan)
        }

        if (tahun) {
            query += ' AND YEAR(scan_timestamp) = ?'
            queryParams.push(tahun)
        }

        const [scans] = await dbase.query(query, queryParams)

        if (scans.length === 0) {
            const response = h.response({
                status: 'fail',
                message: 'No scans found for the user'
            })
            response.code(404)
            return response
        }

        const detailedScans = await Promise.all(scans.map(async (scan) => {
            const [scanDetails] = await dbase.query('SELECT jenis, kalori FROM scan_details WHERE scans_id = ?', [scan.id])

            const localTimestamp = moment(scan.scan_timestamp).tz('Asia/Jakarta').format('YYYY-MM-DD HH:mm:ss')

            return {
                ...scan,
                scan_timestamp: localTimestamp,
                detectedFoods: scanDetails
            }
        }))

        const response = h.response({
            status: 'success',
            message: 'History berhasil diambil',
            data: detailedScans,
        })
        response.code(200)
        return response

    } catch (error) {
        const response = h.response({
            status: 'fail',
            message: 'gagal mengambil history',
        })
        response.code(500)
        return response
    }
}


module.exports = { AccessValidation, SignUp, SignIn, ForgotPasswordSendEmail, ForgotPasswordChangePassword, Logout, ScanImage, GetHistory }