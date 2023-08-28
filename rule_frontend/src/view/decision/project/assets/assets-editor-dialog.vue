<template>
  <div v-if="$hasPerm('project.asset:edit')">

    <el-button icon="el-icon-edit" @click="show = true" >编辑</el-button>

    <el-dialog
      title="文件编辑"
      width="40%"
      :visible.sync="show"
      @open="open"
      @opened="$refs.nameRef.focus()"
      :close-on-click-modal="false"
      :append-to-body="true">
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.native.prevent
        v-loading="assetsLoading">
        <el-form-item label="文件名称：" prop="name">
          <el-input
            v-model="form.name"
            ref="nameRef"
            :maxlength="20"
            show-word-limit
            @input="assetsNameLength"
            @keyup.enter.native="submitEdit"
            placeholder="请输入文件名称">
          </el-input>
        </el-form-item>
        <el-form-item label="文件描述：" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            @input="assetsDescLength"
            @keyup.enter.native="submitEdit"
            :maxlength="100"
            :autosize="{ minRows: 6 }"
            :show-word-limit="true"
            placeholder="请输入文件描述">
          </el-input>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="创建时间：">
              <span>{{$moment(form.createTime).format('YYYY-MM-DD HH:mm')}}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新时间：">
              <span v-if="form.updateTime">{{$moment(form.updateTime).format('YYYY-MM-DD HH:mm')}}</span>
              <span v-else>无</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="show = false" icon="el-icon-error" :disabled="assetsLoading">取 消</el-button>
        <el-button
          @click="lock"
          :disabled="assetsLoading"
          :loading="lockLoading"
          :icon="form.locked ? 'el-icon-unlock' : 'el-icon-lock'"
          v-if="$hasPerm('project.asset:edit')">
          {{form.locked ? '解锁文件' : '锁定文件'}}
        </el-button>
        <el-button
          type="primary"
          icon="el-icon-success"
          :disabled="assetsLoading"
          v-if="$hasPerm('project.asset:edit')"
          @click="submitEdit"
          :loading="editLoading">
          {{editLoading ? '提交中...' : '确 认'}}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "asset-editor-dialog",
  props: {
    assets: Object,
  },
  computed: {
    showDropdown() {
      return ["testcase", "fact", "constant"].indexOf(this.assets.type) === -1
    },
  },
  data() {
    const self = this
    return {
      show: false,
      form: {},
      rules: {
        name: [
          { required: true, message: "请输入文件名称", trigger: "blur" },
          { max: 20, message: "长度不超过20个字符", trigger: "blur" },
          {
            validator: function (rule, value, callback) {
              if (
                value
                && (self.assets.type == 'fact' || self.assets.type == 'constant')
                && (value.indexOf('.') != -1 ||value.indexOf('_') != -1)
              ) {
                callback(new Error("不能包含字符[.]或[_]"));
              } else {
                callback();
              }
            },
          }
        ],
        description: [
          { max: 100, message: "长度不超过100个字符", trigger: "blur" }
        ]
      },
      editLoading: false,
      assetsLoading: false,
      lockLoading: false
    };
  },
  methods: {
    open() {
      this.initAssets();
    },
    initAssets() {
      this.assetsLoading = true;
      this.$axios.get("/assets/" + this.assets.uuid).then(res => {
        this.assetsLoading = false;
        if (res.data.code == 0) {
          this.form = res.data.data;
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "操作失败");
        }
      }).catch(() => {
        this.$message.error("操作失败");
        this.assetsLoading = false;
      });
    },
    assetsNameLength() {
      const length = this.form.name.length;
      if (length == 20)  {
        this.$message({
          message: "已经达到最大长度",
          type: 'warning'
        });
      }
    },
    assetsDescLength() {
      const length = this.form.description.length;
      if (length == 100)  {
        this.$message({
          message: "已经达到最大长度",
          type: 'warning'
        });
      }
    },
    submitEdit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.editLoading = true;
          this.$axios.post("/assets/edit", this.$qs.stringify({
            uuid: this.form.uuid,
            description: this.form.description,
            name: this.form.name,
            dirParentId: this.assets.dirParentId
          })).then(res => {
            this.editLoading = false;
            if (res.data.code == 0) {
              this.show = false;
              this.$message.success("编辑成功");
              this.$emit("refresh", {
                uuid: this.assets.uuid,
                name: this.form.name,
                locked: this.assets.locked,
                type: this.assets.type,
              });
            } else {
              this.$message.error(res.data.msg ? res.data.msg : "编辑失败");
            }
          }).catch(() => {
            this.$message.error("编辑失败");
            this.editLoading = false;
          });
        }
      });
    },
    exportAssets() {
      const that = this;
      this.$axios({
        method: "get",
        url: "/assets/export?uuid=" + this.assets.uuid,
        responseType: "blob",
      }).then(res => {
        if (res.data.type == "application/json") {
          var reader = new FileReader();
          reader.onload = function () {
            const json = JSON.parse(reader.result);
            if (json.code == 401) {
              localStorage.removeItem('hasLogin')
              const from = that.$router.currentRoute.fullPath
              that.$router.push('/login?from=' + from)
            } else {
              that.$message.error(json.msg ? json.msg : "导出失败");
            }
          };
          reader.readAsText(res.data);
        } else {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          a.setAttribute("download", this.assets.name + '.rule');
          document.body.appendChild(a);
          a.click();
        }
      }).catch(e => {
        console.error(e)
        that.$message.error("导出失败");
      });
    },
    lock() {
      this.lockLoading = true;
      const url = this.form.locked ? '/assets/unlock' : '/assets/lock'
      this.$axios.post(url, this.$qs.stringify({
        uuid: this.form.uuid
      })).then(res => {
        this.lockLoading = false;
        if (res.data.code == 0) {
          this.$message.success("操作成功");
          this.form.locked = !this.form.locked;
          this.$emit("refresh", {
            uuid: this.assets.uuid,
            name: this.assets.name,
            locked: this.form.locked,
          });
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "操作失败");
        }
      }).catch(() => {
        this.$message.error("操作失败");
        this.lockLoading = false;
      });
    },
  },
};
</script>

<style scoped>
</style>
