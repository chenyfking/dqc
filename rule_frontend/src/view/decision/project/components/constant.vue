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
        @change="change"
        popper-class="factPopper"
        :options="options"
        :props="cascaderProps">
      </el-cascader>
      <el-button type="text" slot="reference" style="color: rgb(180, 95, 4); padding: 0px;">
        {{text}}
      </el-button>
    </el-popover>
    <span v-else style="color: rgb(180, 95, 4); padding: 0px; font-size: 14px;">
      {{text}}
    </span>
  </div>
</template>

<script>
  export default {
    name: "constant",
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
      }
    },
    computed: {
      text: function() {
        if (!this.value) return '请选择'
        const text = this.$store.getters.getConstantText(this.value.id, this.value.fieldId)
        if (!text) return '请选择'
        return text
      },
      options: function() {
        const getChildren = constant => {
          const children = []
          constant.fields.forEach(field => {
            // 叶子节点判断类型是否匹配
            if (this.$utils.isNotBlank(this.inputType)
              && !this.$utils.isTypeMatch(field.type, this.inputType)) {
              return
            }

            const child = {
              value: field.id,
              label: field.label
            }
            children.push(child)
          })
          return children
        }

        const items = []
        this.$store.state.constants.forEach(constant => {
          items.push({
            value: constant.uuid,
            label: constant.name,
            children: getChildren(constant)
          })
        })
        return items
      }
    },
    watch: {
      inputType: function (newVal, oldVal) {
        if (newVal != oldVal) {
          this.$delete(this.value, 'id')
          this.$delete(this.value, 'fieldId')
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
      change(command) {
        this.showPopover = false
        this.$nextTick(() => {
          this.$set(this.value, 'id', command[0])
          this.$set(this.value, 'fieldId', command[1])
          this.$emit('input', this.value)
          this.$emit('draw')
        })
      },
      popoverShow(){
        this.showCascader = true
        this.$nextTick(()=>{
          this.$refs.cascader.$el.click()
        })
      },
      popoverHide() {
        this.$nextTick(()=>{
          this.showCascader = false
        })
      }
    }
  }
</script>

<style scoped>
</style>
