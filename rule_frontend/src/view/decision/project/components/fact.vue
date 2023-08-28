<template>
  <div style="display: inline-block;" class="factContent">
    <el-popover
      v-if="!readonly"
      @show="popoverShow"
      @hide="popoverHide"
      v-model="showPopover">
      <cascader
        v-if="showCascader"
        filterable
        placeholder="搜索"
        ref="cascader"
        @change="change"
        popper-class="factPopper"
        :options="options"
        :props="cascaderProps">
      </cascader>
      <el-button type="text" slot="reference" style="color: rgb(180, 95, 4); padding: 0px;">
        {{text}}
      </el-button>
    </el-popover>
    <span v-else style="font-size: 14px; color: rgb(180, 95, 4); padding: 0px;">
      {{text}}
    </span>
  </div>
</template>

<script>
  export default {
    name: "fact",
    props: {
      inputType: [String, Object],
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      immediate: Boolean,
      scope: String,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      text: function() {
        if (!this.value) return '请选择'
        const text = this.$store.getters.getFactText(this.value.id, this.value.fieldId)
        return text || '请选择'
      },
      options: function() {
        const getChildren = fact => {
          const children = []
          fact.fields.forEach(field => {
            // 选择fact用来赋值的时候，不应该展示衍生变量
            if (this.scope == 'ASSIGN' && this.$utils.isDerive(field.type)) return

            const leaf = !this.$utils.isObject(field.type)
            // 叶子节点判断类型是否匹配
            if (this.$utils.isNotBlank(this.inputType)
              && leaf
              && !this.$utils.isTypeMatch(field.type, this.inputType)) {
              return
            }

            const child = {
              value: field.id,
              label: field.label
            }
            if (!leaf) {
              const subFact = this.$store.state.factMap[field.subType]
              if (!subFact) return
              child.children = getChildren(subFact)
            }
            children.push(child)
          })
          return children
        }

        const items = []
        this.$store.state.facts.forEach(fact => {
          if (this.$store.state.subFactIds.indexOf(fact.uuid) == -1) {
            items.push({
              value: fact.uuid,
              label: fact.name,
              children: getChildren(fact)
            })
          }
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
          checkStrictly: true,
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
        const id = command[0]
        const fieldId = command.slice(1).join(',')
        if (this.$utils.isNotBlank(this.inputType)) {
          let isObject = false
          if (command.length == 1) {
            isObject = true
          } else {
            const field = this.$store.getters.getFactField(id, fieldId)
            if (this.$utils.isObject(field.type)) {
              isObject = true
            }
          }
          if (isObject && !this.$utils.isTypeMatch('Object', this.inputType)) {
            return
          }
        }

        this.showPopover = false
        this.$nextTick(() => {
          if (this.value.id) {
            this.$emit('change', {id, fieldId}, {id: this.value.id, fieldId: this.value.fieldId})
          } else {
            this.$emit('change', {id, fieldId}, null)
          }
          this.$set(this.value, 'id', id)
          this.$set(this.value, 'fieldId', fieldId)
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
  .el-cascader__suggestion-item:last-child {
    margin-bottom: 4px;
  }
</style>
