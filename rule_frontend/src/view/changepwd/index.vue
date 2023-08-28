<template>
  <el-dialog
    title="修改密码"
    :visible="show"
    @update:visible="cancel"
    width="30%"
    :modal="!force"
    :show-close="!force"
    :close-on-click-modal="!force"
    :close-on-press-escape="!force"
    @opened="$refs.input.focus()"
    @closed="$refs.form.resetFields()">
    <el-alert
      v-if="force"
      type="warning"
      :title="tips"
      show-icon
      style="margin-bottom: 20px;"
      :closable="false">
    </el-alert>
    <el-form ref="form" :rules="rules" :model="form" label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword" class="is-required">
        <el-input
          type="password"
          v-model="form.oldPassword"
          ref="input"
          show-password
          placeholder="请填写旧密码"
          @keyup.enter.native="submit">
        </el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword" class="is-required">
        <el-input
          type="password"
          v-model="form.newPassword"
          show-password
          placeholder="请填写新密码"
          @keyup.enter.native="submit">
        </el-input>
      </el-form-item>
      <el-form-item label="确认新密码" prop="reNewPassword" class="is-required">
        <el-input
          type="password"
          v-model="form.reNewPassword"
          show-password
          placeholder="请确认新密码"
          @keyup.enter.native="submit">
        </el-input>
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button v-if="force" @click="forceLogout" icon="iconfont icontuichu2">注 销</el-button>
      <el-button v-else @click="cancel" icon="el-icon-error">取 消</el-button>
      <el-button type="primary" @click="submit" icon="el-icon-success" :loading="loading">
        {{loading ? '提交中...' : '确 认'}}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    name: "index",
    props: {
      show: Boolean,
      code: {
        type: Number,
        default: 0
      }
    },
    data() {
      const validateOldPassword = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请填写旧密码'));
        } else {
          callback();
        }
      }

      const validateNewPassword = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请填写新密码'));
        } else {
          if (this.form.reNewPassword !== '') {
            this.$refs.form.validateField('reNewPassword');
          }
          callback();
        }
      }

      const validateReNewPassword = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请确认新密码'));
        } else if (value !== this.form.newPassword) {
          callback(new Error('两次填写新密码不一致!'));
        } else {
          callback();
        }
      }
      return {
        form: {
          oldPassword: '',
          newPassword: '',
          reNewPassword: ''
        },
        rules: {
          oldPassword: [
            { validator: validateOldPassword, trigger: "blur" }
          ],
          newPassword: [
            { validator: validateNewPassword, trigger: 'blur' },
            { min: 6, message: "长度至少6位", trigger: "blur" }
          ],
          reNewPassword: [
            { validator: validateReNewPassword, trigger: 'blur' },
            { min: 6, message: "长度至少6位", trigger: "blur" }
          ]
        },
        loading: false,
        force: this.code > 0,
        tips: this.code == 2 ? '初次登录，请修改密码' : '密码过期，请修改密码'
      }
    },
    mounted() {
      if (this.$refs.input) {
        this.$nextTick(() => this.$refs.input.focus())
      }
    },
    methods: {
      cancel() {
        this.$emit('update:show', false)
      },
      submit() {
        this.$refs.form.validate(valid => {
          if (valid) {
            this.loading = true
            this.$axios.post('/user/editmypwd', this.$qs.stringify({
              oldPassword: this.form.oldPassword,
              newPassword: this.form.newPassword
            })).then(res => {
              this.loading = false
              if (res.data.code == 0) {
                this.$message.success('修改成功')
                if (this.force) {
                  localStorage.removeItem('forceChangePwdCode')
                  this.$router.back()
                } else {
                  this.forceLogout()
                }
              } else {
                this.$message.error(res.data.msg ? res.data.msg : '修改失败')
              }
            }).catch(() => {
              this.$message.error('修改失败')
              this.loading = false
            })
          }
        })
      },
      forceLogout() {
        this.$axios.post('logout').then(res => {
          this.$store.commit('resetUser')
          localStorage.removeItem('hasLogin')
          this.$router.push('/login')
        })
      }
    }
  }
</script>

<style scoped>
</style>
