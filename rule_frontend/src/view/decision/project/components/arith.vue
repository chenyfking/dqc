<template>
  <div style="display: inline-block;">
    <dropdown
      :style="{margin: (value.type ? '0px 5px' : 0)}"
      @command="changeType"
      v-if="!readonly && Object.keys(items).length > 0">
      <span class="el-dropdown-link">
        <template>
          <b v-if="value.type">{{text}}</b>
          <i v-else class="el-icon-caret-bottom"></i>
        </template>
      </span>
      <dropdown-menu slot="dropdown">
        <dropdown-item v-for="(e, i) in Object.keys(items)" :key="i" :command="e">{{items[e]}}</dropdown-item>
      </dropdown-menu>
    </dropdown>
    <span v-else-if="value.type" style="font-size: 14px; margin: 0px 5px;">{{text}}</span>

    <span style="font-size: 14px;" v-if="value.parentheses">(</span>
    <expression
      v-if="value.type"
      v-model="value.right"
      :input-type="inputType"
      :loop-target="loopTarget"
      :immediate="immediate"
      :readonly="readonly">
    </expression>
    <span style="font-size: 14px;" v-if="value.parentheses">)</span>

    <arith
      v-model="value.arith"
      v-if="showArith"
      :input-type="inputType"
      :loop-target="loopTarget"
      @draw="$emit('draw')"
      :readonly="readonly">
    </arith>
  </div>
</template>

<script>
  export default {
    name: "arith",
    props: {
      inputType: [String, Object],
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      readonly: {
        type: Boolean,
        default: false
      },
      loopTarget: Object
    },
    data() {
      return {
        immediate: false
      }
    },
    computed: {
      text: function() {
        return this.items[this.value.type]
      },
      showArith: function() {
        const type = this.inputType
        if (type == 'Date' || type == 'Boolean') {
          this.$delete(this.value, 'arith')
          return false
        }
        return this.value.parentheses
      },
      items: function() {
        let items
        if (this.$utils.isString(this.inputType)) {
          items = {
            ADD: '+',
          }
        } else if (this.$utils.isNumber(this.inputType)) {
          items = {
            ADD: '+',
            SUB: '-',
            MUL: 'x',
            DIV: '÷'
          }
        }
        if (this.value.type) {
          if (this.value.parentheses) {
            items['DEL_PARENTHESES'] = '删除括号'
          } else {
            items['ADD_PARENTHESES'] = '括号'
          }
        }
        if (this.$utils.isNotBlank(this.value.type)) {
          if (this.value.parentheses) {
            items['DEL'] = '删除符号'
          } else {
            items['DEL'] = '删除'
          }
        }
        return items || {}
      }
    },
    methods: {
      changeType(command) {
        if (command == 'DEL') {
          this.$delete(this.value, 'type')
          this.$delete(this.value, 'right')
          this.$delete(this.value, 'parentheses')
        } else if (command == 'ADD_PARENTHESES') {
          this.$set(this.value, 'parentheses', true)
          this.$emit('input', this.value)
        } else if (command == 'DEL_PARENTHESES') {
          this.$delete(this.value, 'parentheses')
          this.$emit('input', this.value)
        } else if (command != this.value.type) {
          this.$set(this.value, 'type', command)
          this.immediate = true
          this.$emit('input', this.value)
        }
      }
    }
  }
</script>

<style scoped>

</style>
