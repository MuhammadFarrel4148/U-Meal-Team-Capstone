const { users, foods, otp, blacklistedToken } = require('./database')
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

const blacklistToken = (token) => {
    // Tambahkan token ke daftar token yang diblacklist
    blacklistedToken.push(token);
};

const SignUp = (request, h) => {
    const { username, email, phonenumber, password } = request.payload
    const CheckEmail = users.filter((c) => c.email === email)[0]

    //Check apakah email sudah terpakai
    if(CheckEmail !== undefined){
        const response = h.response({
            status: 'fail',
            message: 'Gagal untuk menambahkan, email sudah digunakan'
        })
        response.code(400)
        return response
    }

    const id = nanoid(16)
    const InsertUser = {
        username, email, phonenumber, password, id,
    }

    users.push(InsertUser)
    const IsSuccess = users.filter((u) => u.id === id).length > 0

    //Check apakah user berhasil ditambahkan
    if(IsSuccess) {
        const response = h.response({
            status: 'success',
            message: 'user berhasil ditambahkan',
            data: {
                users : users.map(({ id, username, email, phonenumber }) => ({ id, username, email, phonenumber }))
            }
        })
        response.code(201)
        return response
    }

    const response = h.response({
        status: 'fail',
        message: 'user gagal ditambahkan'
    })

    response.code(500)
    return response
}

const SignIn = (request, h) => {
    const { email, password } = request.payload
    const user = users.find(u => u.email === email && u.password === password)
    
    //Check user 
    if(user !== undefined) {
        const token = GenerateToken(user)
    
        const response = h.response({
            status: 'success',
            message: 'Berhasil login ke user',
            token: token
        })
        response.code(200)
        return response
    }
    
    const response = h.response({
        status: 'fail',
        message: 'Invalid email or password'
    })
    response.code(404)
    return response   
}

const ForgotPasswordSendEmail = async(request, h) => {
    const { email } = request.payload
    const CheckEmail = users.filter((c) => c.email === email)[0]

    //Check Email
    if (CheckEmail !== undefined) {
        const transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            }
        });

        const codeOtp = nanoid(8);
        otp.push({ email, codeOtp })

    
        await transporter.sendMail({
            from: 'U-Meal Application',
            to: email,
            subject: 'Kode OTP Verification',
            text: `This is your OTP code ${codeOtp}`
        });

        const response = h.response({
            status: 'success',
            message: 'Check your email to reset the password'
        });
        response.code(200)
        return response
    }

    const response = h.response({
        status: 'fail',
        message: 'Email tidak ditemukan'
    });
    response.code(404)
    return response
}

const ForgotPasswordChangePassword = (request, h) => {
    const { codeOtp, newPassword } = request.payload
    const verifyOTP = otp.find(verify => verify.codeOtp === codeOtp)
    
    if (verifyOTP) {
        const user = users.find((u) => u.email === verifyOTP.email)

        if (user) {
            user.password = newPassword;

            // Remove the used OTP
            otp.splice(otp.indexOf(verifyOTP), 1)

            const response = h.response({
                status: 'success',
                message: 'Password has been reset successfully'
            })
            response.code(200)
            return response
        }

        const response = h.response({
            status: 'fail',
            message: 'User not found'
        })
        response.code(404)
        return response
    }

    const response = h.response({
        status: 'fail',
        message: 'Invalid OTP'
    });
    response.code(400)
    return response
}

const Logout = (request, h) => {
    const { authorization } = request.headers

    if (!authorization && CheckToken === undefined) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        })
        response.code(401)
        return response
    }

    const token = authorization.split(' ')[1];
    const CheckToken = blacklistedToken.find(b => b === token)

    if (CheckToken !== undefined) {
        const response = h.response({
            status: 'fail',
            message: 'Unauthorized'
        });
        response.code(401)
        return response
    }

    try {
        const decodedToken = jwt.verify(token, JWT_SECRET)

        // Panggil fungsi blacklistToken dengan meneruskan token yang didecode
        blacklistToken(decodedToken)

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
        response.code(401);
        return response;
    }
}

//sementara pakai data rekayasa,database sekalian nunggu dari ML
const GetFoods = (request, h) => {
    const foodsAndKalori = foods.map(food => {
        const totalCalories = food.protein + food.karbohidrat + food.serat
        return { ...food, totalCalories }
    })

    const response = h.response({
        status: 'success',
        data: {
            foods: foodsAndKalori
        }
    });
    response.code(200)
    return response
}

const GetTotalKalori = (request, h) => {
    totalKalori = foods.reduce((acc, food) => acc + food.protein + food.karbohidrat + food.serat, totalKalori)

    const response = h.response({
        status: 'success',
        data: {
            totalKalori
        }
    })
    response.code(200)
    return response
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