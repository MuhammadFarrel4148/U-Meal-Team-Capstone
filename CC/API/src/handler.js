const dbase = require('./database')
const { nanoid } = require('nanoid')
const jwt = require('jsonwebtoken')
const nodemailer = require('nodemailer')
const JWT_SECRET = process.env.JWT_SECRET

let totalKalori = 0

const GenerateToken = (user) => {
    const token = jwt.sign({id: user.id, email: user.email}, JWT_SECRET, {expiresIn: '1h'})
    return token
}

const AccessValidation = (request, h) => {
    const authorization = request.headers.authorization
    
    if(!authorization) {
        const response = h.response({
            status: 'fail',
            message: 'Token tidak ditemukan'
        })
        response.code(401)
        return response
    }

    const token = authorization.split(' ')[1]

    try {
        const jwtDecode = jwt.verify(token, JWT_SECRET)
        return jwtDecode

    } catch(error) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response
    }
}

const blacklistToken = async (token) => {
    await dbase.query('INSERT INTO blacklisted_tokens (token) VALUES (?)', [token])
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
            });
            response.code(400)
            return response
        }

        // Tambahkan pengguna baru
        const id = nanoid()
        const [result] = await dbase.query('INSERT INTO users (id, username, email, phonenumber, password) VALUES (?, ?, ?, ?, ?)', [id, username, email, phonenumber, password])

        if (result.affectedRows === 1) {
            const response = h.response({
                status: 'success',
                message: 'User berhasil ditambahkan',
                data: {
                    user: {
                        id,
                        username,
                        email,
                        phonenumber
                    }
                }
            });
            response.code(201)
            return response
        } else {
            throw new Error('User gagal ditambahkan');
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'User gagal ditambahkan'
        });
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
                token: token
            });
            response.code(200)
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'Invalid email or password'
            });
            response.code(404)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal login'
        });
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
            });

            await transporter.sendMail({
                from: 'U-Meal Application',
                to: email,
                subject: 'Kode OTP Verification',
                text: `This is your OTP code: ${codeotp}`
            });

            const response = h.response({
                status: 'success',
                message: 'Periksa email Anda untuk mereset kata sandi'
            });
            response.code(200)
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'Email tidak ditemukan'
            });
            response.code(404)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal mengirim email untuk reset kata sandi'
        });
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
            await dbase.query('DELETE FROM otp WHERE codeotp = ?', [codeotp]);

            const response = h.response({
                status: 'success',
                message: 'Kata sandi berhasil direset'
            });
            response.code(200);
            return response
        } else {
            const response = h.response({
                status: 'fail',
                message: 'OTP tidak valid'
            });
            response.code(400)
            return response
        }
    } catch (error) {
        console.error('Error:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal mereset kata sandi'
        });
        response.code(500)
        return response
    }
}

const Logout = async (request, h) => {
    const authorization = request.headers.authorization;

    if (!authorization) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        });
        response.code(401);
        return response;
    }

    const token = authorization.split(' ')[1];

    const [blacklistedTokens] = await dbase.query('SELECT token FROM blacklisted_tokens WHERE token = ?', [token]);
    if (blacklistedTokens.length > 0) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        });
        response.code(401);
        return response;
    }

    try {
        jwt.verify(token, JWT_SECRET);
        await blacklistToken(token);

        const response = h.response({
            status: 'success',
            message: 'Logout berhasil'
        });
        response.code(200);
        return response;
    } catch (error) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        });
        response.code(401);
        return response;
    }
}

const GetFoods = async (request, h) => {
    try {
        const [foods] = await dbase.query('SELECT name, protein, karbohidrat, serat FROM foods')
        const foodsAndKalori = foods.map(food => {
            const totalCalories = food.protein + food.karbohidrat + food.serat;
            return { ...food, totalCalories }
        });

        const response = h.response({
            status: 'success',
            data: {
                foods: foodsAndKalori
            }
        });
        response.code(200)
        return response
    } catch (error) {
        console.error('Error getting foods:', error)
        const response = h.response({
            status: 'fail',
            message: 'Gagal mengambil data makanan'
        });
        response.code(500)
        return response
    }
}

const GetTotalKalori = async (request, h) => {
    try {
        const [foods] = await dbase.query('SELECT protein, karbohidrat, serat FROM foods');
        const totalKalori = foods.reduce((acc, food) => acc + food.protein + food.karbohidrat + food.serat, 0)

        const response = h.response({
            status: 'success',
            data: {
                totalKalori
            }
        });
        response.code(200)
        return response
    } catch (error) {
        console.error('Error getting total calories:', error);
        const response = h.response({
            status: 'fail',
            message: 'Gagal menghitung total kalori'
        });
        response.code(500)
        return response
    }
}

const allowedImageTypes = ['image/jpeg', 'image/png']

const UploadImage = async (request, h) => {
    const { image } = request.payload
    const mimeType = image.hapi.headers['content-type']

    if (!allowedImageTypes.includes(mimeType)) {
        const response = h.response({
            status: 'fail',
            message: 'Hanya file gambar dengan format jpg, jpeg, dan png yang diperbolehkan untuk diupload'
        });
        response.code(400)
        return response
    }

    // Ini kan ntar upload images ga nyimpan gembar,jadi langsung diproses
    //tinggal tambah fungsi buat scan gambarnya,ntar next meet dibahas

    const response = h.response({
        status: 'success',
        message: 'Gambar berhasil discan',
        data: {
            filename: image.hapi.filename,
            fileType: mimeType
        }
    });
    response.code(200)
    return response
}

module.exports = { AccessValidation, SignUp, SignIn, ForgotPasswordSendEmail, ForgotPasswordChangePassword, Logout, GetFoods, GetTotalKalori, UploadImage }