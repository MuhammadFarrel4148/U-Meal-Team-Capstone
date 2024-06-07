const { AccessValidation, SignUp, SignIn, ForgotPasswordSendEmail, ForgotPasswordChangePassword, Logout, GetFoods, GetTotalKalori, ScanImage  } = require('./handler')

const routes = [
    {
        method: 'POST',
        path: '/signup',
        handler: SignUp
    },

    {
        method: 'POST',
        path: '/signin',
        handler: SignIn
    },

    {
        method: 'POST',
        path: '/forgotpassword',
        handler: ForgotPasswordSendEmail
    },

    {
        method: 'POST',
        path: '/changepassword',
        handler: ForgotPasswordChangePassword
    },

    {
        method: 'POST',
        path: '/logout',
        handler: Logout
    },

    {
        method: 'GET',
        path: '/foods',
        handler: (request, h) => AccessValidation(request, h) && GetFoods(request, h)
    },

    {
        method: 'GET',
        path: '/kalori',
        handler: (request, h) => AccessValidation(request, h) && GetTotalKalori(request, h)
    },
    {
        method: 'POST',
        path: '/scan',
        options: {
            payload: {
                output: 'stream',
                allow: 'multipart/form-data',
                maxBytes: 10485760, // 10 MB
                parse: true,
                multipart: true
            },  
        },
        handler: ScanImage //(request, h) => AccessValidation(request, h) && ScanImage(request, h)
    }
]

module.exports = routes