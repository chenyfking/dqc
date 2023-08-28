import Axios from '@/axios'

const loadBom = (commit, payload) => {
  Axios.axios.get('/bom', {
    params: {
      projectUuid: payload.projectUuid
    }
  }).then(res => {
    if (res.data.code == 0) {
      commit('refreshBoms', res.data.data)
      payload.callback && payload.callback()
    }
  })
}

const loadFunction = commit => {
  Axios.axios.get('/function').then(res => {
    if (res.data.code == 0) {
      commit('refreshBoms', {functions: res.data.data})
    }
  })
}

const loadModel = commit => {
  Axios.axios.get('/aimodel').then(res => {
    if (res.data.code == 0) {
      res.data.data.forEach(e => {
        e.text = e.modelName
      })
      commit('refreshBoms', {models: res.data.data})
    }
  })
}

const loadThirdApi = commit => {
  Axios.axios.get('/thirdapi').then(res => {
    if (res.data.code == 0) {
      commit('refreshBoms', {thirdapis: res.data.data})
    }
  })
}

export default {
  loadBoms({commit}, payload) {
    loadBom(commit, payload)
    !payload.ignoreFunc && loadFunction(commit)
    !payload.ignoreModel && loadModel(commit)
    !payload.ignoreThirdApi && loadThirdApi(commit)
  },
  getUserProfile({commit}){
    return new Promise((resolve,reject)=>{
      Axios.axios.get('/user/profile').then(res=>{
        if(res.data.code==0){
          commit('setUser',res.data.data)
          resolve(res.data.data)
        }else{
          reject(res)
        }
      }).catch(e=>{
        reject(e)
      })
    })
  },
}
