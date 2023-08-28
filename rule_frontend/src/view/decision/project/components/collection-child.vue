<template>
  <div style="display: inline-block;">
    <template v-if="!readonly">
      <span style="font-size: 14px; color: rgb(180, 95, 4);" v-if="baseType">{{text}}</span>
      <el-popover
        v-else
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
    </template>
    <span style="font-size: 14px; color: rgb(180, 95, 4);" v-else>{{text}}</span>
  </div>
</template>

<script>
  export default {
    name: "collection-child",
    props: {
      inputType: [String, Object],
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      loopTarget: Object,
      immediate: Boolean,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      field: function() {
        return this.$store.getters.getFactField(this.loopTarget.id, this.loopTarget.fieldId)
      },
      baseType: function() {
        const field = this.$store.getters.getFactField(this.loopTarget.id, this.loopTarget.fieldId)
        return this.$utils.isBaseType(field.subType)
      },
      text: function() {
        if (this.baseType) return this.field.label
        if (this.value.id) return this.$store.getters.getFactText(this.value.id, this.value.fieldId)
        return '请选择'
      },
      options: function() {
        const getChildren = fact => {
          const children = []
          fact.fields.forEach(field => {
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
        const fact = this.$store.state.factMap[this.field.subType]
        items.push({
          value: fact.uuid,
          label: fact.name,
          children: getChildren(fact)
        })
        return items
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
          this.$set(this.value, 'id', id)
          this.$set(this.value, 'fieldId', fieldId)
          this.$emit('input', this.value)
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
