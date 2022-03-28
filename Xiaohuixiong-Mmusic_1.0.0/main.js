import Vue from 'vue'
import App from './App'
import store from '@/store'
import Toast from '@/utils/toast'

Vue.config.productionTip = false
Vue.prototype.$store = store
Vue.prototype.$toast = new Toast()


App.mpType = 'app'

const app = new Vue({
	store,
    ...App
})
app.$mount()
