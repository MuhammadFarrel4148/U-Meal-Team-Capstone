const tf = require('@tensorflow/tfjs-node')
const InputError = require('../exceptions/InputError')

const predictHandler = async(model, image) => {
    try {
        const tensor = tf.node
            .decodeJpeg(image)
            .resizeNearestNeighbor([300, 300])
            .expandDims()
            .toFloat()

        const prediction = model.predict(tensor)
        
        console.log('Prediction type:', typeof prediction);
        console.log('Prediction keys:', Object.keys(prediction));

        const labels = ['Ayam', 'Daging', 'Ikan', 'Nasi', 'Sayur', 'Tahu', 'Telur', 'Tempe']

        let predictedLabels = [];

        for (const key of Object.keys(prediction)) {
            const data = prediction[key].arraySync() 

            // Log the data for debugging
            console.log(`Data for key ${key}:`, data)

            // Check if data is an array or a single value
            if (Array.isArray(data)) {
                data.forEach((value, index) => {
                    if (value >= 0) { 
                        predictedLabels.push(labels[index])
                    }
                })
            } else {
                if (data >= 0) { 
                    predictedLabels.push(labels[key])
                }
            }
        }

        return { results: predictedLabels }

    } catch (error) {
        throw new InputError(`Terjadi kesalahan input: ${error.message}`)
    }
};

module.exports = predictHandler
