import valueInput from "@/view/decision/project/components/input.vue"
import func from "@/view/decision/project/components/func"
import model from "@/view/decision/project/components/model"
import fact from "@/view/decision/project/components/fact"
import constant from "@/view/decision/project/components/constant"
import attr from "@/view/decision/project/components/attr"
import arith from "@/view/decision/project/components/arith"
import funcList from "@/view/decision/project/components/func-list"
import funcSet from "@/view/decision/project/components/func-set"
import op from "@/view/decision/project/components/op"
import expression from "@/view/decision/project/components/expression"
import action from "@/view/decision/project/components/action"
import thirdApi from "@/view/decision/project/components/thirdapi"
import collectionChild from "@/view/decision/project/components/collection-child"
import testCollection from "@/view/decision/project/knowledgepackage/test-collection"
import testCollectionChild from "@/view/decision/project/knowledgepackage/test-collection-child"
import testObject from "@/view/decision/project/knowledgepackage/test-object"
import assetsViewer from "@/view/decision/project/assets/assets-view"
import assetsEditorDialog from '@/view/decision/project/assets/assets-editor-dialog'
import saveBtn from '@/view/decision/project/assets/save-btn'
import assetsVersionDialog from '@/view/decision/project/assets/assets-version-dialog'
import ruleflowTrace from '@/view/decision/project/components/ruleflow-trace'
import cascader from './cascader/cascader'
import dropdown from './dropdown/dropdown'
import dropdownMenu from './dropdown/dropdown-menu'
import dropdownItem from './dropdown/dropdown-item'

const install = function(Vue, opts = {}) {
  Vue.component('value-input', valueInput)
  Vue.component('func', func)
  Vue.component('model', model)
  Vue.component('fact', fact)
  Vue.component('constant', constant)
  Vue.component('attr', attr)
  Vue.component('arith', arith)
  Vue.component('func-list', funcList)
  Vue.component('func-set', funcSet)
  Vue.component('op', op)
  Vue.component('expression', expression)
  Vue.component('action', action)
  Vue.component('collection-child', collectionChild)
  Vue.component('test-collection', testCollection)
  Vue.component('test-collection-child', testCollectionChild)
  Vue.component('test-object', testObject)
  Vue.component('thirdapi', thirdApi)
  Vue.component('assets-view', assetsViewer)
  Vue.component('assets-editor-dialog', assetsEditorDialog)
  Vue.component('save-btn', saveBtn)
  Vue.component('assets-version-dialog', assetsVersionDialog)
  Vue.component('ruleflow-trace', ruleflowTrace)
  Vue.component('cascader', cascader)
  Vue.component('dropdown', dropdown)
  Vue.component('dropdown-menu', dropdownMenu)
  Vue.component('dropdown-item', dropdownItem)
}

export default {
  install
}
