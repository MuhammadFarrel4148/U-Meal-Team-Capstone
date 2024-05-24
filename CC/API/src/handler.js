const { users, foods } = require('./database')
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

const Logout = (request, h) => {
    const {id} = request.params
    const userIndex = users.findIndex ((u) => u.id === id)
    if (userIndex !== -1){
        users.splice(userIndex, 1)
        const response = h.response({
            status: 'Success',
            message: 'Logout berhasil'
        })
        response.code(200)
        return response
    }
    const response = h.response({
        status: 'fail',
        message: 'User tidak ditemukan'
    })
    response.code(404)
    return response
}
const CRUDFood = (request, h) => {
    const { method } = request;
    const { id } = request.params;

    if (method === 'post') {
        const { makanan, protein, karbohidrat, serat } = request.payload
        const foodId = nanoid(16)
        const newFood = { id: foodId, makanan, protein, karbohidrat, serat }
        
        foods.push(newFood)
        
        const response = h.response({
            status: 'success',
            message: 'Makanan berhasil ditambahkan',
            data: newFood
        })
        response.code(201)
        return response

    } else if (method === 'get') {
        if (id) {
            const food = foods.find(f => f.id === id)
            if (food) {
                const response = h.response ({
                    status: 'Success',
                    message: 'Data makanan berhasil diambil',
                    data: food
                })
                response.code(200)
                return response
            } else {
                const response = h.response({
                    status: 'fail',
                    message: 'Makanan tidak ditemukan'
                })
                response.code(404)
                return response
            }
        } else {
            const response = h.response({
                status: 'success',
                message: 'Data semua makanan berhasil diambil',
                data: foods
            })
            response.code(200)
            return response
        }
    } else if (method === 'put') {
        const { makanan, protein, karbohidrat, serat } = request.payload
        const foodIndex = foods.findIndex(f => f.id === id)
        if (foodIndex !== -1) {
            foods[foodIndex] = {id , makanan, protein, karbohidrat, serat }
            const response = h.response({
                status: 'success',
                message: 'Makanan berhasil diupdate',
                data: foods[foodIndex]
            })
            response.code(200)
            return response
        }
        const response = h.response({
            status: 'fail',
            message: 'Makanan tidak ditemukan'
        })
        response.code(404)
        return response
    } else if (method === 'delete'){
        const foodIndex = foods.findIndex(f => f.id === id)
        if (foodIndex !== -1){
            foods.splice(foodIndex, 1)
            const response = h.response({
                status: 'success',
                message: 'Makanan berhasil dihapus'
            })
            response.code(200)
            return response
        }
        const response = h.response({
            status: 'fail',
            message: 'Makanan gagal ditemukan'
        })
        response.code(404)
        return response
    }
}


module.exports = { SignUp, SignIn, ForgotPassword, Logout, CRUDFood }