<template>
  <el-dialog title="保存新版本" width="30%" :close-on-click-modal="false"
             :visible.sync="show" @close="versionDesc = ''" @closed="$emit('update:active', false)">
    <el-form ref="form" :rules="rules" label-width="80px">
      <el-form-item label="文件名称">
        <el-input v-model="assets.name" disabled></el-input>
      </el-form-item>
      <el-form-item label="版本描述" prop="desc">
        <el-input v-model="versionDesc" type="textarea"
                  :maxlength="255" :show-word-limit="true" placeholder="请输入版本描述"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
      <el-button type="primary" @click="$emit('submit', versionDesc)" icon="el-icon-success" :loading="loading">
        {{loading ? '提交中...' : '确 认'}}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
  export default {
    name: "assets-new-version-dialog",
    props: {
      active: Boolean,
      assets: Object,
      loading: Boolean
    },
    watch: {
      active(newVal) {
        this.show = newVal
      }
    },
    data() {
      return {
        show: this.active,
        versionDesc: '',
        rules: {
          desc: [
            {max: 255, message: '长度不超过255个字符', trigger: 'blur'}
          ]
        }
      }
    }
  }
</script>

<style scoped>

</style>
