<template>
  <el-popconfirm v-if="$hasPerm('project.asset:del')" title="确定删除？" @onConfirm="del">
    <el-button
      slot="reference"
      icon="el-icon-delete"
      :loading="loading">
      {{loading ? '提交中...' : '删除'}}
    </el-button>
  </el-popconfirm>
</template>

<script>
export default {
  name: "delete-btn",
  props: {
    assets: Object
  },
  data() {
    return {
      loading: false
    };
  },
  methods: {
    del() {
      this.loading = true;
      this.$axios.post("/assets/delete", this.$qs.stringify({
        uuid: this.assets.uuid
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success("删除成功");
          this.$emit("delete",  this.assets.uuid);
          this.$store.commit('setRecyle',true)
          if (this.assets.type == 'fact' || this.assets.type == 'constant') {
            this.$store.dispatch('loadBoms', {
              projectUuid: this.$route.params.uuid,
              ignoreFunc: true,
              ignoreModel: true,
              ignoreThidApi: true
            })
          }
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "删除失败");
        }
        this.loading = false;
      }).catch(() => {
        this.$message.error("删除失败");
        this.loading = false;
      });
    }
  }
};
</script>

<style scoped>
</style>
