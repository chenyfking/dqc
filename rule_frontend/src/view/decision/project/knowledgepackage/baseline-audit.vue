<template>
  <el-dialog
    title="审核基线"
    width="30%"
    :visible.sync="show"
    :close-on-click-modal="false"
    @closed="closed">
    <el-form :model="form" ref="form" label-width="120px" @submit.native.prevent>
      <el-form-item label="是否通过" prop="state">
        <el-select v-model="form.state" placeholder="请选择">
          <el-option label="通过" value="2"></el-option>
          <el-option label="不通过" value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="描述理由" prop="auditReason" v-if="form.state == '4'">
        <el-input
          type="textarea"
          v-model="form.auditReason"
          placeholder="请填写不通过理由"
          :autosize="{ minRows: 6 }"
          :maxlength="100"
          show-word-limit>
        </el-input>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
      <el-button
        type="primary"
        @click="submit"
        icon="el-icon-success"
        :loading="loading">
        {{loading ? '提交中...' : '确 定'}}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "edit-dialog",
  props: {
    active: Boolean,
    auditData: Object
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    },
    auditData: function(newVal) {
      this.auditData = newVal;
    }
  },
  data() {
    return {
      loading: false,
      show: this.active,
      form: {
        state: "2",
        auditReason: ''
      },
      rules: {
        auditReason: [
          { max: 100, message: "长度不超过100个字符", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    closed() {
      this.$refs.form.resetFields();
      this.$emit("update:active", false);
      this.$emit("closed");
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true;
          let params = {};
          params.uuid = this.auditData.uuid;
          params.state = this.form.state;
          params.auditReason = this.form.auditReason;
          params.packageUuid = this.auditData.packageUuid;
          this.$axios.post("/baseline/audit", this.$qs.stringify(params)).then(res => {
            this.loading = false;
            if (res.data.code == 0) {
              this.show = false;
              this.$emit("reload");
               this.$message.success("审批成功")
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "审批失败");
            }
          }).catch(() => {
            this.loading = false;
            this.$message.error("审批失败");
          });
        }
      });
    }
  }
};
</script>

<style scoped>
</style>
