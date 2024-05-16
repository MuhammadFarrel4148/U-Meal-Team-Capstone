const { SignUp, SignIn, ForgotPassword, Logout } = require('./handler')

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
    }
]

module.exports = routes