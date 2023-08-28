<template>
  <change-pwd :show.sync="showChangePwd" :code="code" v-if="code > 0"></change-pwd>
</template>

<script>
  import changePwd from './index'

  export default {
    name: "force",
    components: { changePwd },
    data() {
      return {
        showChangePwd: false,
        code: 0
      }
    },
    mounted() {
      const code = localStorage.getItem('forceChangePwdCode')
      if (!code) {
        this.$router.back()
        return
      }
      this.code = parseInt(code)
      this.showChangePwd = true
    }
  }
</script>

<style scoped>

</style>
