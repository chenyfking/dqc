<template>
  <el-dropdown-item
    :command="command"
    :disabled="disabled"
    :divided="divided"
    :icon="icon">
    <slot></slot>
  </el-dropdown-item>
</template>

<script>
  export default {
    name: "dropdown-item",
    props: {
      command: {},
      disabled: Boolean,
      divided: Boolean,
      icon: String
    }
  }
</script>

<style scoped>

</style>
