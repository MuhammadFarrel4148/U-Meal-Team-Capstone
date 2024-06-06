require('dotenv').config();

const Hapi = require('@hapi/hapi')
const routes = require('./routes')
const loadModel = require('../services/loadModel')
const InputError = require('../exceptions/InputError')

const init = async() => {
    const server = Hapi.server({
        port: 8080,
        host: process.env.NODE_ENV !== 'production' ? 'localhost' : '0.0.0.0',
        routes: {
            cors: {
                origin: ['*']
            }
        }
    })

    const model = await loadModel()
    server.app.model = model

    server.route(routes)

    server.ext('onPreResponse', function(request, h) {
        const response = request.response

        if (response instanceof InputError) {
            const newResponse = h.response({
                status: 'fail',
                message: 'Terjadi kesalahan dalam melakukan prediksi'
            })
            newResponse.code(response.statusCode)
            return newResponse;
        }

        if(response.isBoom) {
            const newResponse = h.response({
                status: 'fail',
                message: `Payload content length greater than maximum allowed: 1000000`
            })
            newResponse.code(response.output.statusCode)
            return newResponse
        }

        return h.continue
    })

    await server.start()
    console.log(`Berjalan di ${server.info.uri}`)
}

init()