<template>
</template>

<script>
  export default {
    name: "login",
    data() {
      return {
        from: this.$route.query.from,
        unAuthenticated: this.$route.params.unAuthenticated
      }
    },
    mounted() {
      if (this.unAuthenticated) {
        // 调用接口401跳转过来的
        this.login();
      } else {
        // 手动输入或者重定向跳转过来的

        this.$store.dispatch('getUserProfile').then(() => {
          // 接口获取用户信息成功，认为已登录
          if (this.from) {
            this.$router.push(decodeURIComponent(this.from));
          } else {
            this.$router.push('/');
          }
        }).catch(res => {
          // 获取用户信息失败
          if (!res.data || res.data.code == -1) {
            // 接口报错了
            this.$router.push('/500');
          } else if (res.data.code == 401) {
            this.login();
          }
        });
      }
    },
    methods: {
      login() {
        this.$axios.get('/api/dqcindex').then(res => {
          if (res.data.code == 0) {
            location.href = res.data.data 
          }
        });
      }
    }
  }
</script>

<style scoped>
</style>
