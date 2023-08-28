<template>
  <div style="display: inline-block;">
    <el-popover
      v-if="!readonly"
      @show="popoverShow"
      @hide="popoverHide"
      v-model="showPopover">
      <el-cascader
        v-if="showCascader"
        filterable
        placeholder="搜索"
        ref="cascader"
        @change="changeFunc"
        popper-class="factPopper"
        :options="options"
        :props="cascaderProps">
      </el-cascader>
      <el-button type="text" slot="reference" style="color: darkcyan; padding: 0px;">
        {{value.method ? value.method : '请选择'}}
      </el-button>
    </el-popover>
    <span v-else style="color: darkcyan; padding: 0px; font-size: 14px;">{{value.method}}</span>

    <template v-if="value.method">
      <span style="font-size: 14px;">(</span>
      <div v-for="(e, i) in value.params" :key="i" style="display: inline-block">
        <span style="color: darkcyan; font-size: 14px;" v-if="!value.params[i].value">{{e.name}}: </span>
        <expression
          v-model="value.params[i].value"
          :input-type="e.type"
          @draw="$emit('draw')"
          :loop-target="loopTarget"
          :readonly="readonly">
        </expression>
        <span style="font-size: 14px;" v-if="value.params.length - 1 > i">,&nbsp;&nbsp;&nbsp;</span>
      </div>
      <span style="font-size: 14px;">)</span>
    </template>
  </div>
</template>

<script>
  export default {
    name: "func",
    props: {
      inputType: [String, Object],
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      immediate: Boolean,
      readonly: {
        type: Boolean,
        default: false
      },
      loopTarget: Object
    },
    computed: {
      options: function() {
        const getChildren = func => {
          const children = []
          func.methods.forEach(method => {
            // 叶子节点判断类型是否匹配
            if (this.$utils.isNotBlank(this.inputType)
              && !this.$utils.isTypeMatch(method.returnType, this.inputType)) {
              return
            }

            const child = {
              value: method.name,
              label: method.name
            }
            children.push(child)
          })
          return children
        }

        const items = []
        this.$store.state.functions.forEach(func => {
          items.push({
            value: func.name,
            label: func.name,
            children: getChildren(func)
          })
        })
        return items
      }
    },
    watch: {
      inputType: function (newVal, oldVal) {
        if (newVal != oldVal) {
          this.$delete(this.value, 'name')
          this.$delete(this.value, 'method')
          this.$delete(this.value, 'params')
          this.$emit('input', this.value)
        }
      }
    },
    data() {
      return {
        showPopover: false,
        showCascader: false,
        cascaderProps: {
          expandTrigger: 'hover'
        }
      }
    },
    mounted() {
      if (this.immediate) {
        this.showPopover = true
      }
    },
    methods: {
      changeFunc(command) {
        this.showPopover = false
        this.$nextTick(() => {
          const method = this.$store.getters.getFuncMethod(command[0], command[1])
          if (this.value.params) {
            method.params.forEach((e, i) => {
              if (i < this.value.params.length) {
                const param = this.value.params[i]
                if (param.type == e.type) {
                  e.value = param.value
                }
              }
            })
          }
          this.$set(this.value, 'name', command[0])
          this.$set(this.value, 'method', command[1])
          this.$set(this.value, 'params', method.params)
          this.$emit('input', this.value)
          this.$emit('draw')
        })

      },
      popoverShow() {
        this.showCascader = true
        this.$nextTick(() => {
          this.$refs.cascader.$el.click()
        })
      },
      popoverHide() {
        this.$nextTick(() => {
          this.showCascader = false
        })
      }
    }
  }
</script>

<style scoped>
</style>
