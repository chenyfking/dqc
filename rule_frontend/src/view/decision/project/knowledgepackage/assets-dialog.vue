<template>
  <div>
    <el-dialog
      title="规则文件列表"
      :top="$dialogTop"
      :visible.sync="show"
      @open="loadAssets"
      @closed="closed"
      width="60%"
      :close-on-click-modal="false">
      <div class="toolbar" v-if="isAdd">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="showAssetsAddDialog = true">
          添加规则文件
        </el-button>
      </div>
      <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="assets">
        <el-table-column label="文件名称" prop="name"></el-table-column>
        <el-table-column prop="type" label="文件类型">
          <template slot-scope="scope">
            <span>{{$utils.getAssetsTypeText(scope.row.type)}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="versionNo" label="版本号">
          <template
            slot-scope="scope"
          >{{scope.row.versionNo||(scope.row.versionNo==0)?'V'+scope.row.versionNo:""}}</template>
        </el-table-column>
        <el-table-column label="版本描述">
              <template slot-scope="scope" v-if="scope.row.versionDes">
                <span v-if="scope.row.versionDes.length<12">{{ scope.row.versionDes }}</span>
                <el-tooltip v-else placement="top" :content="scope.row.versionDes">
                  <span>{{ (scope.row.versionDes.length>12)?scope.row.versionDes.substring(0,12)+"...":scope.row.versionDes }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
        <el-table-column label="创建时间">
          <template slot-scope="scope">{{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button @click.stop="viewAssets(scope.row)" type="text">查看</el-button>
            <el-button
              v-if="isAdd"
              @click.stop="delAssets(scope.row)"
              type="text">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" v-if="isAdd">
        <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" @click="saveAssets" icon="el-icon-success" :loading="saveLoading">
          {{saveLoading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
      <assets-add-dialog
        :active.sync="showAssetsAddDialog"
        :assets="assets"
        @view="viewAssets"
        @add="submitAddAssets"
        @closed="loadAssets">
      </assets-add-dialog>
    </el-dialog>
    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show">
    </assets-view>
  </div>
</template>

<script>
import assetsAddDialog from "./assets-add-dialog";
export default {
  name: "assets-dialog",
  props: {
    active: Boolean,
    isAdd: Boolean,
    baseline: Object
  },
  components: {
    "assets-add-dialog": assetsAddDialog,
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    }
  },
  data() {
    return {
      show: this.active,
      tableLoading: false,
      assets: [],
      showAssetsAddDialog: false,
      viewingAssets: {
        uuid: "",
        version: 0,
        show: false
      },
      saveLoading: false
    };
  },
  methods: {
    closed() {
      this.assets = [];
      this.$emit("update:active", false);
      this.$emit("closed");
    },
    loadAssets() {
      if (this.isAdd) return;
      const pkg = this.$store.state.pkg
      this.assets = [];
      this.tableLoading = true;
      this.$axios.get("/knowledgepackage/assets", {
        params: {
          uuid: pkg.packageUuid?pkg.packageUuid:pkg.uuid,
          baselineVersion: this.baseline.versionNo
        }
      }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          res.data.data.forEach(item=>{
           item.versionDes= item.description
          })
          this.assets = res.data.data;
        }
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    viewAssets(row) {
      this.viewingAssets.uuid = row.uuid;
      this.viewingAssets.version = row.versionNo;
      this.viewingAssets.show = true;
    },
    saveAssets() {
      if (this.assets.length <= 0) {
        this.$message.warning('请选择资源文件')
        return
      }

      let assets = [];
      this.assets.forEach(item => {
        assets.push({
          assetsUuid: item.uuid,
          assetsVersion: item.versionNo
        });
      });
      assets = JSON.stringify(assets);
      this.saveLoading = true
      this.$axios.post("/baseline/save", this.$qs.stringify({
        packageUuid: this.$store.state.pkg.uuid,
        assets
      })).then(res => {
        this.saveLoading = false
        if (res.data.code == 0) {
          this.$message.success("添加成功");
          this.show = false;
          this.$emit('reload')
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "添加失败");
        }
      }).catch(() => {
        this.$message.error("添加失败");
        this.saveLoading = false
      });
    },
    submitAddAssets(data) {
      let repeat = false;
      this.assets.forEach(item => {
        if (item.uuid == data.uuid) {
          this.$message.warning("请勿重复添加");
          repeat = true;
        }
      });
      if (!repeat) {
        data.versionNo  = data.select
        this.$message.success("添加成功");
        this.assets.push(data);
      }
    },
    delAssets(row) {
      this.assets.forEach((item, index) => {
        if (item.uuid == row.uuid) {
          this.assets.splice(index, 1);
        }
      });
    },
    test() {
      this.$emit("test", { uuid: this.$store.state.pkg.uuid });
    }
  }
};
</script>

<style scoped>
</style>
