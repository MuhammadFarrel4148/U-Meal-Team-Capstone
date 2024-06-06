require('dotenv').config();

const Hapi = require('@hapi/hapi')
const routes = require('./routes')

const init = async() => {
    const server = Hapi.server({
        port: 8080,
        host: process.env.NODE_ENV !== 'production' ? 'localhost' : '0.0.0.0'
    })

    server.route(routes)
    await server.start()
    console.log(`Berjalan di ${server.info.uri}`)
}

init()