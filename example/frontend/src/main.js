import Vue from 'vue'
import App from './App.vue'
import axios from 'axios'
import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.prototype.$http = axios
Vue.config.productionTip = false
Vue.use(BootstrapVue)    
Vue.use(IconsPlugin)

new Vue({
	render: h => h(App),
	axios
}).$mount('#app')