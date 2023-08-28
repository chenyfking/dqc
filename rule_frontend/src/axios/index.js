import axios from 'axios'
import router from "../router"
import qs from 'qs'
import { Message } from 'element-ui'
// import VueCookie from "vue-cookies"

// let baseUrl = 'http://local-web:33005/gaea'
let baseUrl = window.location.protocol + '//' + window.location.host + '/gaea'
let openapiUrl =   window.location.protocol + '//' + window.location.host + '/openapi'
window.baseUrl = baseUrl
window.openapiUrl = openapiUrl

const install = function(Vue, opts = {}) {
  axios.defaults.baseURL = baseUrl
  axios.interceptors.request.use(
    config => {
      config.crossDomain = true;
      config.withCredentials = true;
      //判定dqc 用户标志
      // if(store.state.Authorization) {
      //   config.headers.Authorization = store.state.Authorization
      // } else {
      //   let authorization = VueCookie.get("Authorization");
      //   if (authorization) {
      //     store.state.Authorization = authorization
      //     config.headers.Authorization = authorization
      //   }
      // }
      return config;
    },
    error => {
      return Promise.reject(error)
    }
  );
  axios.interceptors.response.use(
    response => {
      if (response.data.code == 401) {
        localStorage.removeItem('hasLogin')
        let from = router.currentRoute.fullPath
        if (!from.startsWith('/login')) {
          router.push('/login?from=' + from);
        }
        return response;
      } else if (response.data.code == -2) {
        router.push('/nolicense');
      } else if (response.data.code == 2 || response.data.code == 3) {
        localStorage.setItem('forceChangePwdCode', response.data.code)
        router.push('/forcechangepwd')
      } else if (response.data.code == 333) {
        router.push('/dqclogin')
      }

      //dqc 用户标志
      // var curTime = new Date();
      //   var expire = new Date(curTime.setMinutes(curTime.getMinutes() + 30));
      //   if(store.state.Authorization) {
      //     VueCookie.set('Authorization', store.state.Authorization, expire)
      //   } else {
      //     let authorization = VueCookie.get("Authorization");
      //     if (authorization) {
      //       VueCookie.set('Authorization', authorization, expire)
      //     }
      //   }
      //   let newAuthorization = response.headers.authorization
      // if(newAuthorization) {
      //   store.state.Authorization = newAuthorization;
      //   var curTime = new Date();
      //   var expire = new Date(curTime.setMinutes(curTime.getMinutes() + 30));
      //   VueCookie.set('Authorization', newAuthorization, expire)
      // }

      return response;
      },
      
    
    error => {
      if (error.response) {
        console.log(error.response);
      } else if (error.message == 'Network Error') {
        Message.closeAll()
        Message({type:'error', message:"请求失败，请检查接口服务是否启用", showClose:true})
      }
    }
  )

  Vue.prototype.$axios = axios
  Vue.prototype.$qs = qs
}

export default {
  install,
  axios,
  qs
}
