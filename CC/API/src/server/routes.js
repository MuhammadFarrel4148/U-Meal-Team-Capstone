const { AccessValidation, SignUp, SignIn, ForgotPasswordSendEmail, ForgotPasswordChangePassword, Logout, ScanImage, GetHistory  } = require('./handler')


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
        path: '/scanimage',
        options: {
            payload: {
                output: 'stream',
                allow: 'multipart/form-data',
                maxBytes: 10485760, // 10 MB
                parse: true,
                multipart: true
            },
            pre: [{ method: AccessValidation }]
        },
        handler: ScanImage
    },

    {
        method: 'GET',
        path: '/history/{id}',
        options: {
            pre: [{ method: AccessValidation }]
        },
        handler: GetHistory
    }
]

module.exports = routes