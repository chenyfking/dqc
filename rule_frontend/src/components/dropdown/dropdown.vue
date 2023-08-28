<template>
  <el-dropdown
    :type="type"
    :size="size"
    :split-button="splitButton"
    :placement="placement"
    :trigger="trigger"
    :hide-on-click="hideOnClick"
    :show-timeout="showTimeout"
    :hide-timeout="hideTimeout"
    :tabindex="tabindex"
    @click="click"
    @command="command"
    @visible-change="visibleChange"
    ref="dropdown">
    <slot></slot>
    <slot name="dropdown"></slot>
  </el-dropdown>
</template>

<script>
  export default {
    name: "dropdown",
    props: {
      type: String,
      size: {
        type: String,
        default: ''
      },
      splitButton: Boolean,
      placement: {
        type: String,
        default: 'bottom'
      },
      trigger: {
        type: String,
        default: 'click'
      },
      hideOnClick: {
        type: Boolean,
        default: true
      },
      showTimeout: {
        type: Number,
        default: 250
      },
      hideTimeout: {
        type: Number,
        default: 150
      },
      tabindex: {
        type: Number,
        default: 0
      }
    },
    methods: {
      click(e) {
        this.$emit('click', e)
      },
      command(e) {
        this.$emit('command', e)
      },
      visibleChange(e) {
        this.$emit('visible-change', e)
      },
      show() {
        this.$refs.dropdown.show()
      }
    }
  }
</script>

<style scoped>

</style>
