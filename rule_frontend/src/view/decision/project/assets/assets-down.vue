<template>
   <el-button type="success" icon="el-icon-download" @click="download">导出</el-button>
</template>

<script>
export default {
  name: "assetsDown",
  props: {
    uuid: String
  },
  methods: {
    download() {
      window.open(this.$axios.defaults.baseURL + '/rules/export?uuid=' + this.uuid)
    }
  }
};
</script>

<style scoped>
</style>
