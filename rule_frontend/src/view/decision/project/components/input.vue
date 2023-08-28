<template>
  <div style="display: inline-block">
    <template v-if="!readonly">
      <dropdown v-if="$utils.isBoolean(type)" @command="change" ref="input">
        <span class="el-dropdown-link" style="color: rgb(180, 95, 4);">{{text}}</span>
        <dropdown-menu slot="dropdown">
          <dropdown-item command="true">是</dropdown-item>
          <dropdown-item command="false">否</dropdown-item>
        </dropdown-menu>
      </dropdown>
      <el-popover v-else ref="popover" @after-enter="$refs.input.focus()">
        <el-input-number
          v-if="$utils.isNumber(type)"
          :controls="false"
          ref="input"
          v-model.trim="value.value"
          clearable
          @keyup.enter.native="change"
          :step-strictly="inputInteger()"
          @blur="change">
        </el-input-number>

        <el-date-picker v-else-if="$utils.isDate(type)" v-model="value.value"
                        type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="请选择" align="center" ref="input"
                        @change="change">
        </el-date-picker>

        <el-input v-else v-model="value.value" ref="input" clearable
                  @keyup.enter.native="change" @blur="change"></el-input>

        <el-button type="text" slot="reference" style="color: rgb(180, 95, 4); padding: 0px;">
          {{text}}
        </el-button>
      </el-popover>
    </template>
    <template v-else>
      <span style="color: rgb(180, 95, 4); padding: 0px; font-size: 14px;">{{text}}</span>
    </template>
  </div>
</template>

<script>
  export default {
    name: "valueInput",
    props: {
      inputType: {
        type: [String, Object],
        default: 'String'
      },
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
      type: function() {
        if (this.inputType) {
          const dotIndex = this.inputType.indexOf('.')
          if (dotIndex != -1) return this.inputType.substring(dotIndex + 1)
        }
        return this.inputType
      },
      text: function() {
        if (this.$utils.isBlank(this.value.value)) return '请选择'
        if (this.$utils.isBoolean(this.type)) {
          return this.value.value === 'true' ? '是' : '否'
        }
        return this.value.value
      }
    },
    mounted() {
      if (this.immediate) {
        this.show()
      }
    },
    watch: {
      inputType: function (newVal, oldVal) {
        if (newVal != oldVal) {
          this.$delete(this.value, 'value')
          this.$emit('input', this.value)
        }
      }
    },
    methods: {
      show() {
        if (this.$utils.isBoolean(this.type)) {
          this.$refs.input.show()
        } else {
          this.$refs.popover.doShow()
        }
      },
      change(val) {
        if (this.$utils.isBoolean(this.type)) {
          this.$set(this.value, 'value', val)
        } else {
          this.$refs.popover.doClose()
        }
        if (this.inputType) {
          this.$set(this.value, 'type', this.inputType)
        }
        if (typeof this.value.value == 'string') {
          this.value.value = this.value.value.trim()
        }
        this.$emit('input', this.value)
        this.$emit('draw')
      },
      /**
       * 是否只能输入整数
       */
      inputInteger() {
        return ['Integer', 'Long'].indexOf(this.inputType) != -1
      }
    }
  }
</script>

<style scoped>
</style>
