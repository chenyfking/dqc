<template>
  <div style="display: inline-block; font-size: 0px; -webkit-text-size-adjust: none;">
    <dropdown @command="changeType" ref="input" v-if="!readonly">
      <span class="el-dropdown-link" v-if="value.type"><i class="el-icon-caret-bottom"></i></span>
      <span style="cursor: pointer" v-else>请选择</span>
      <dropdown-menu slot="dropdown">
        <dropdown-item v-for="(e, i) in Object.keys(types)" :key="i" :command="e">{{types[e]}}</dropdown-item>
        <dropdown-item command="PARENTHESES" v-if="!value.parentheses && this.value.type">添加括号</dropdown-item>
        <dropdown-item command="PARENTHESES" v-if="value.parentheses">删除括号</dropdown-item>
      </dropdown-menu>
    </dropdown>

    <span style="font-size: 14px;" v-if="value.parentheses">(</span>
    <!--输入框组件-->
    <value-input v-if="value.type == 'INPUT'"
                 v-model="value.input"
                 :input-type="inputType"
                 @draw="$emit('draw')"
                 :immediate="childImmediate"
                 :readonly="readonly">
    </value-input>
    <!--数据结构组件-->
    <fact v-else-if="value.type == 'FACT'"
          v-model="value.fact"
          :input-type="inputType"
          @draw="$emit('draw')"
          :immediate="childImmediate"
          :readonly="readonly">
    </fact>
    <!--枚举常量组件-->
    <constant v-else-if="value.type == 'CONSTANT'"
              v-model="value.constant"
              :input-type="inputType"
              @draw="$emit('draw')"
              :immediate="childImmediate"
              :readonly="readonly">
    </constant>
    <!--函数组件-->
    <func v-else-if="value.type == 'FUNC'"
          v-model="value.func"
          :input-type="inputType"
          @draw="$emit('draw')"
          :immediate="childImmediate"
          :loop-target="loopTarget"
          :readonly="readonly">
    </func>
    <!--集合元素组件-->
    <collection-child v-else-if="value.type == 'COLLECTION_CHILD'"
                      v-model="value.collectionChild"
                      :input-type="inputType"
                      :loop-target="loopTarget"
                      :immediate="childImmediate"
                      :readonly="readonly">
    </collection-child>
    <!--括号内算术组件-->
    <arith v-model="value.arith"
           v-if="showArith"
           :input-type="realInputType"
           :loop-target="loopTarget"
           @draw="$emit('draw')"
           :readonly="readonly">
    </arith>
    <span style="font-size: 14px;" v-if="value.parentheses">)</span>
    <!--括号外算术组件-->
    <arith v-model="value.parenArith"
           v-if="showArith && value.parentheses"
           :input-type="realInputType"
           @draw="$emit('draw')"
           :readonly="readonly">
    </arith>
  </div>
</template>

<script>
  export default {
    name: "expression",
    props: {
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      inputType: {
        type: [String, Object],
        required: false
      },
      immediate: Boolean,
      loopTarget: Object,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        childImmediate: false
      }
    },
    computed: {
      types: function() {
        const types = {}
        const inputType = this.inputType
        if (this.$utils.isBlank(inputType) || this.$utils.isBaseType(inputType)) {
          // 没有类型或者是基本类型，支持所有表达式
          types['INPUT'] = '输入值'
          types['FACT'] = '数据结构'
          types['CONSTANT'] = '枚举常量'
          types['FUNC'] = '执行函数'
        } else {
          // 不是基本类型，不支持输入和枚举常量
          types['FACT'] = '数据结构'
          types['FUNC'] = '执行函数'
        }
        if (this.loopTarget) {
          types['COLLECTION_CHILD'] = '集合对象'
        }
        return types
      },
      showArith: function() {
        const type = this.inputType || this.$store.getters.getExpressionType(this.value)
        if (this.$utils.isString(type) || this.$utils.isNumber(type)) {
          return true
        }
        this.$delete(this.value, 'arith')
        return false
      },
      realInputType: function() {
        if (this.$utils.isNotBlank(this.inputType)) return this.inputType
        return this.$store.getters.getExpressionType(this.value, this.loopTarget)
      }
    },
    mounted() {
      if (this.immediate) {
        this.show()
      }
    },
    methods: {
      show() {
        this.$refs.input.show()
      },
      changeType(command) {
        if (command == 'PARENTHESES') {
          if (this.value.parentheses) { // 删除括号
            this.$set(this.value, 'parentheses', false)
            this.$delete(this.value, 'parenArith')
          } else {
            this.$set(this.value, 'parentheses', true)
          }
          this.$emit('input', this.value)
        } else if (command != this.value.type) {
          this.$delete(this.value, 'input')
          this.$delete(this.value, 'fact')
          this.$delete(this.value, 'constant')
          this.$delete(this.value, 'func')
          this.$delete(this.value, 'model')
          this.$delete(this.value, 'arith')

          this.$set(this.value, 'type', command)
          this.$emit('input', this.value)
          this.$emit('draw')
          this.childImmediate = true
        }
      }
    }
  }
</script>

<style scoped>

</style>
