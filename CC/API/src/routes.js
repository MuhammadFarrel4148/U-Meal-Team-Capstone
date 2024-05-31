const { AccessValidation, SignUp, SignIn, ForgotPasswordSendEmail, ForgotPasswordChangePassword, Logout, CRUDFood,  } = require('./handler')

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
        method: 'POST',
        path: '/kalori',
        handler: (request, h) => AccessValidation(request, h) && CRUDFood(request, h)
    },

    {
        method: 'GET',
        path: '/kalori',
        handler: (request, h) => AccessValidation(request, h) && CRUDFood(request, h)
    },

    {
        method: 'GET',
        path: '/kalori/{id}',
        handler: (request, h) => AccessValidation(request, h) && CRUDFood(request, h)
    },

    {
        method: 'DELETE',
        path: '/kalori/{id}',
        handler: (request, h) => AccessValidation(request, h) && CRUDFood(request, h)
    },
]

module.exports = routes