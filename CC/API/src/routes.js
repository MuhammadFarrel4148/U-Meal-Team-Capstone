const { SignUp, SignIn, ForgotPassword, Logout, CRUDFood } = require('./handler')

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
        handler: ForgotPassword
    },
    {
        method: 'POST',
        path: '/logout/{id}',
        handler: Logout
    },
    {
        method: 'POST',
        path: '/kalori',
        handler: CRUDFood
    },
    {
        method: 'GET',
        path: '/kalori',
        handler: CRUDFood
    },
    {
        method: 'GET',
        path: '/kalori/{id}',
        handler: CRUDFood
    },
    {
        method: 'DELETE',
        path: '/kalori/{id}',
        handler: CRUDFood
    },
]

module.exports = routes