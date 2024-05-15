const users = require('./database')
const { nanoid } = require('nanoid')

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
        id, username, email, phonenumber, password
    }

    users.push(InsertUser)
    const IsSuccess = users.filter((u) => u.id === id).length > 0

    //Check apakah user berhasil ditambahkan
    if(IsSuccess) {
        const response = h.response({
            status: 'success',
            message: 'user berhasil ditambahkan',
            data: {
                users : users.map(({ username, email, phonenumber }) => ({ username, email, phonenumber }))
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
        const response = h.reponse({
            status: 'success',
            message: 'Berhasil login ke user'
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

const ForgotPassword = (request, h) => {
    const { email } = request.payload
    const CheckEmail = users.filter((c) => c.email === email)[0]

    //Check Email
    if(CheckEmail !== undefined) {
        const response = h.response({
            status: 'success',
            message: 'Check your email to reset the password'
        })
        response.code(200)
        return response
    }
    
    const response = h.response({
        status: 'fail',
        message: 'Email tidak ditemukan'
    })
    response.code(404)
    return response
}

module.exports = { SignUp, SignIn, ForgotPassword }