<template>
  <div style="display: inline-block;">
    <el-popover
      v-if="!readonly"
      @show="popoverShow"
      @hide="popoverHide"
      v-model="showPopover">
      <el-select
        v-if="showSelector"
        v-model="uuid"
        ref="selector"
        @change="change"
        filterable
        placeholder="请选择">
        <el-option
          v-for="e in $store.state.models"
          :key="e.uuid"
          :label="e.modelName"
          :value="e.uuid">
        </el-option>
      </el-select>
      <el-button type="text" slot="reference" style="color: darkcyan; padding: 0px;">
        {{text}}
      </el-button>
    </el-popover>
    <span v-else style="color: darkcyan; font-size: 14px; padding: 0px;">{{text}}</span>

    <template v-if="value.id">
      (
      <span style="color: darkcyan; font-size: 14px;" v-if="!value.input">输入: </span>
      <fact v-model="value.input" input-type="Object" :readonly="readonly"></fact>
      ,
      <span style="color: darkcyan; font-size: 14px;" v-if="!value.output">输出: </span>
      <fact v-model="value.output" input-type="Object" :readonly="readonly"></fact>
      )
    </template>
  </div>
</template>

<script>
  export default {
    name: "model",
    props: {
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
        const text = this.$store.getters.getModelName(this.value.id)
        if (!text) return '请选择'
        return text
      }
    },
    data() {
      return {
        showPopover: false,
        showSelector: false,
        uuid: ''
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
          this.$set(this.value, 'id', command)
          this.$emit('input', this.value)
          this.$emit('draw')
        })
      },
      popoverShow() {
        this.showSelector = true
        this.$nextTick(() => {
          this.$refs.selector.toggleMenu()
        })
      },
      popoverHide() {
        this.$nextTick(() => {
          this.showSelector = false
        })
      }
    }
  }
</script>

<style scoped>

</style>
