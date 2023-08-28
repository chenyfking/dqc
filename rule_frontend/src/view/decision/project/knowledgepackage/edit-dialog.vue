<template>
  <el-dialog :title="form.uuid ? '编辑知识包' : '添加知识包'" width="30%"
             :visible.sync="show" :close-on-click-modal="false"
             @open="open" @closed="closed">
    <el-form :model="form" :rules="rules" ref="form" label-width="80px" @submit.native.prevent>
      <el-form-item label="名称" prop="name">
        <el-input v-model.trim="form.name" ref="input" @keyup.enter.native="submit" placeholder="知识包名称，不可重名"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input type="textarea" v-model="form.description" placeholder="还没有描述-这个知识包是做什么的？"
                  :autosize="{ minRows: 6 }" :maxlength="100" :show-word-limit="true"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
      <el-button type="primary" @click="submit" icon="el-icon-success" :loading="loading">
        {{loading ? '提交中...' : '确 定'}}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
  export default {
    name: "edit-dialog",
    props: {
      active: Boolean
    },
    watch: {
      active: function(newVal) {
        this.show = newVal
      }
    },
    data() {
      return {
        loading: false,
        show: this.active,
        form: {},
        rules: {
          name: [
            {required: true, message: '请输入名称', trigger: 'blur'},
            {max: 20, message: '长度不超过20个字符', trigger: 'blur'}
          ],
          description: [
            {max: 100, message: '长度不超过100个字符', trigger: 'blur'}
          ]
        }
      }
    },
    methods: {
      open() {
        this.form = JSON.parse(JSON.stringify(this.$store.state.pkg))
        this.$nextTick(function() {
          this.$refs.input.focus()
        })
      },
      closed() {
        this.$refs.form.resetFields()
        this.$emit('update:active', false)
      },
      submit() {
        this.$refs.form.validate(valid => {
          if (valid) {
            let url = this.form.uuid ? '/knowledgepackage/edit' : '/knowledgepackage/add'
            let params = {}
            if (this.form.uuid) {
              params.uuid = this.form.uuid
            } else {
              params.projectUuid = this.$route.params.uuid
            }
            params.name = this.form.name
            params.description = this.form.description
            this.loading = true;
            this.$axios.post(url, this.$qs.stringify(params)).then(res => {
              this.loading = false
              if (res.data.code == 0) {
                this.show = false
                this.$emit('submit')
                this.$message.success("保存成功")
              } else {
                this.$message.error(res.data.msg ? res.data.msg : '操作失败')
              }
            }).catch(() => {
              this.loading = false
              this.$message.error('操作失败')
            })
          }
        })
      }
    }
  }
</script>

<style scoped>

</style>
