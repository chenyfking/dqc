<template>
  <div v-if="$hasPerm('project.asset:edit')">
    <dropdown
      split-button
      type="primary"
      @click="save(false)"
      v-if="newVersion">
      <i class="iconfont iconbaocun"></i>{{saveLoading ? '提交中...' : '保存'}}
      <dropdown-menu slot="dropdown">
        <dropdown-item
          @click.native="save(true)"
          icon="iconfont iconVersionupdaterule-copy">
          保存新版本
        </dropdown-item>
        <!-- <dropdown-item
          @click.native="saveTemplate"
          v-if="assets.type=='guidedrule' || assets.type=='ruletable'|| assets.type=='ruletree'|| assets.type=='scorecard'"
          icon="el-icon-money">
          保存为模板
        </dropdown-item> -->
        <dropdown-item
          icon="el-icon-view"
          @click.native="showVersionDialog = true">
          查看历史版本
        </dropdown-item>
      </dropdown-menu>
    </dropdown>

    <el-button
      type="primary"
      @click="save(false)"
      :disabled="saveLoading"
      icon="iconfont iconbaocun"
      v-else>
      {{saveLoading ? '提交中...' : '保存'}}
    </el-button>

    <el-dialog
      v-if="newVersion"
      title="保存新版本"
      width="30%"
      append-to-body
      :visible.sync="showNewVersionDialog"
      @close="versionDesc = null">
      <el-form label-width="80px">
        <el-form-item label="文件名称">
          <el-input v-model="assets.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="版本描述" prop="desc">
          <el-input
            v-model="versionDesc"
            type="textarea"
            :maxlength="100"
            show-word-limit
            placeholder="请输入版本描述">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showNewVersionDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="doSaveNewVersion"
          icon="el-icon-success"
          :loading="saveNewVersionLoading">
          {{saveNewVersionLoading ? '提交中...' : '确 认'}}
        </el-button>
      </div>
    </el-dialog>

    <assets-version-dialog
      v-if="newVersion"
      :assets="assets"
      :active.sync="showVersionDialog"
      @change="changeVersion">
    </assets-version-dialog>
  </div>
</template>

<script>
export default {
  name: "save-btn",
  props: {
    assets: Object,
    newVersion: {
      type: Boolean,
      default: function() {
        return true
      }
    }
  },
  data() {
    return {
      saveLoading: false,
      saveNewVersionLoading: false,
      showNewVersionDialog: false,
      content: null,
      versionDesc: null,
      callback: null,
      showVersionDialog: false
    };
  },
  methods: {
    save(newVersion) {
      if (this.saveLoading) return;

      this.$emit("callback", (content, callback) => {
        this.content = content;
        this.callback = callback;

        if (!newVersion) {
          this.doSave();
        } else {
          this.showNewVersionDialog = true;
        }
      });
    },
    saveTemplate() {
      if (this.saveLoading) return;

      this.$emit("callback", (content, callback) => {
        this.content = content;
        this.callback = callback;
        this.doSaveTemplate();
      });
    },
    doSave() {
      this.saveLoading = true;
      this.$axios.post("/assets/savecontent", this.$qs.stringify({
        uuid: this.assets.uuid,
        content: this.content
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success("保存成功");
          typeof this.callback === "function" && this.callback();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "保存失败");
        }
      }).catch(() => {
        this.$message.error("保存失败");
      }).then(() => {
        this.saveLoading = false;
      });
    },
    //把资源文件保存为模板
    doSaveTemplate() {
      this.saveLoading = true;
      this.$axios.post("/template", this.$qs.stringify({
        assetsUuid: this.assets.uuid,
        name: this.assets.name
      })).then(res => {
        if (res.data.code == 0) {
          this.$message.success("保存成功");
          const data = {
            name: res.data.data.name,
            uuid: res.data.data.uuid,
            dirParentId: 'tpl_'+res.data.data.type,
            type: 'tpl_'+res.data.data.type,
            leaf:true
          }
          this.$store.commit('setAddTreeNode', data)
          this.$emit("template");
          typeof this.callback === "function" && this.callback();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "保存失败");
        }
      }).catch(() => {
        this.$message.error("保存失败");
      }).then(() => {
        this.saveLoading = false;
      });
    },
    doSaveNewVersion() {
      this.saveNewVersionLoading = true;
      this.$axios.post("/assets/version/new", this.$qs.stringify({
        uuid: this.assets.uuid,
        content: this.content,
        versionDesc: this.versionDesc
      })).then(res => {
        if (res.data.code == 0) {
          this.showNewVersionDialog = false;
          this.$message.success("保存成功");
          typeof this.callback === "function" && this.callback();
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "保存失败");
        }
      }).catch(() => {
        this.$message.error("保存失败");
      }).then(() => {
        this.saveNewVersionLoading = false;
      });
    },
    changeVersion(content) {
      this.$emit('refresh', {
        uuid: this.assets.uuid,
        changeVersion: true,
        content: content
      })
    },
    saveContent(content, callback, newVersion = false) {
      this.content = content
      this.callback = callback

      if (!newVersion) {
        this.doSave()
      } else {
        this.showNewVersionDialog = true
      }
    }
  }
};
</script>

<style scoped>
</style>
