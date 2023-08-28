<template>
  <dropdown @command="changeOp" v-if="!readonly">
    <span class="el-dropdown-link" style="color: rgb(255, 0, 0)">{{text}}</span>
    <dropdown-menu slot="dropdown" v-if="items.length > 0">
      <dropdown-item v-for="(e, i) in items" :key="i" :command="e.value">{{e.text}}</dropdown-item>
    </dropdown-menu>
    <dropdown-menu slot="dropdown" v-else>
      <dropdown-item disabled>无</dropdown-item>
    </dropdown-menu>
  </dropdown>
  <span style="color: rgb(255, 0, 0)" v-else>{{text}}</span>
</template>

<script>
  export default {
    name: "op",
    props: {
      value: String,
      inputType: [String, Object],
      readonly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      items: function() {
        return this.$op.filterItems(this.inputType)
      },
      text: function() {
        return this.$op.getText(this.op) || '操作符'
      }
    },
    watch: {
      inputType: function(newVal, oldVal) {
        if (newVal != oldVal && this.op) {
          // 类型变化，检测是否匹配当前操作符，如果不匹配设置为默认第一个或者null
          let valid = false, items = this.items
          items.some(e => {
            if (e.value == this.op) {
              valid = true
              return true
            }
          })
          if (!valid) {
            if (items.length > 0) {
              this.changeOp(items[0].value)
            } else {
              this.changeOp(null)
            }
          }
        }
      }
    },
    data() {
      return {
        op: this.value
      }
    },
    mounted() {
      if (!this.op) {
        const items = this.items
        if (items.length > 0) {
          this.changeOp(items[0].value)
        }
      }
    },
    methods: {
      changeOp(command) {
        this.op = command
        this.$emit('input', command)
        this.$emit('draw')
      }
    }
  }
</script>

<style scoped>
  div {
    display: inline-block;
  }
</style>
