import Utils from '@/common/utils'

export default {
  setPkg(state, pkg) {
    state.pkg = pkg
  },
  setPkgs(state, pkgs) {
    state.pkgs = pkgs
  },
  setsAssetsLink(state, assetsLink) {
    state.assetsLink = assetsLink
  },
  setAddTreeNode(state,addTreeNode){
    state.addTreeNode = addTreeNode
  },
  setRecyle(state,reloadRecyle){
    state.reloadRecyle = reloadRecyle
  },
  refreshBoms(state, payload) {
    if ('facts' in payload) {
      state.factMap = {}
      state.fieldMap = {}
      state.subFactIds = []
      payload.facts.forEach(e => {
        state.factMap[e.uuid] = e
        e.fields.forEach(f => {
          state.fieldMap[e.uuid + '.' + f.id] = f
          if (Utils.utils.isObject(f.type)) {
            if (state.subFactIds.indexOf(f.subType) == -1) {
              state.subFactIds.push(f.subType)
            }
          } else if (Utils.utils.isCollection(f.type) && Utils.utils.isObject(f.subType)) {
            if (state.subFactIds.indexOf(f.subType) == -1) {
              state.subFactIds.push(f.subType)
            }
          }
        })
      })
      state.facts = payload.facts
    }
    if ('constants' in payload) {
      state.constants = payload.constants
    }
    if ('functions' in payload) {
      state.functions = payload.functions
    }
    if ('models' in payload) {
      state.models = payload.models
    }
    if ('thirdapis' in payload) {
      state.thirdapis = payload.thirdapis
    }
  },
  setUser(state, user) {
    state.user = user
  },
  resetUser(state) {
    state.user = null
  }
}
