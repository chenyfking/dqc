import Vue from 'vue'
import router from './router'

import "./assets/css/index.css"

import './element-ui/element-ui.config';

import App from './App'
import axios from './axios'
import utils from './common/utils'
import conjunction from './common/conjunction'
import op from './common/op'
import lhs from './common/lhs'
import attr from './common/attr'
import components from './components'

import store from './vuex/index.js'
import moment from 'moment'

import contextmenu from './common/contextmenu'
import { hasPerm, hasAnyPerm } from './common/permission'

Vue.directive('contextmenu', contextmenu)

Vue.use(axios)
Vue.use(components)
Vue.use(utils)
Vue.use(conjunction)
Vue.use(op)
Vue.use(lhs)
Vue.use(attr)

Vue.config.productionTip = false

Vue.prototype.$moment = moment;
Vue.prototype.$hasPerm = hasPerm;
Vue.prototype.$hasAnyPerm = hasAnyPerm;

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
