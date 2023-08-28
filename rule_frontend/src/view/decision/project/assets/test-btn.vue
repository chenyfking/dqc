<template>
  <div v-if="$hasPerm('project.pkg:run')">
    <el-button
      icon="el-icon-lightning"
      @click="test"
      :loading="loading">
      测试
    </el-button>
    <test-dialog :active.sync="showTestDialog"></test-dialog>
  </div>
</template>

<script>
  import testDialog from '../knowledgepackage/test-dialog'

  export default {
    name: "test-btn",
    components: {
      'test-dialog': testDialog
    },
    props: {
      assets: Object
    },
    data() {
      return {
        loading: false,
        showTestDialog: false
      }
    },
    methods: {
      test() {
        this.loading = true
        this.$axios.post('/knowledgepackage/temptest', this.$qs.stringify({
          assetsUuid: this.assets.uuid
        })).then(res => {
          if (res.data.code == 0) {
            this.showTestDialog = true
            this.$store.commit('setPkg', res.data.data)
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '操作失败')
          }
        }).catch(() => {
          this.loading = false
        }).then(() => {
          this.loading = false
        })
      }
    }
  }
</script>

<style scoped>

</style>
