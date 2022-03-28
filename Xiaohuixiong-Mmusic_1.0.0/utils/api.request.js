import HttpRequest from './axios.js'
import config from './config'

const serverUrl = process.env.NODE_ENV === 'development' ? config.serverUrl.dev : config.serverUrl.pro;

const axios = new HttpRequest(serverUrl)

export default axios
