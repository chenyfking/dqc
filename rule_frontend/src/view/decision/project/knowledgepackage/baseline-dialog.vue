<template>
  <div>
    <el-dialog
      title="版本列表"
      :top="$dialogTop"
      :visible.sync="show"
      @closed="closed"
      :close-on-click-modal="false"
      width="70%">
      <div class="toolbar">
        <el-button
          type="primary"
          v-if="$hasPerm('project.pkg.baseline:add')"
          @click="addBaseline"
          icon="el-icon-plus">
          新增版本
        </el-button>
        <el-button
          @click="openDiff"
          icon="el-icon-document-copy">
          对比差异
        </el-button>
      </div>
      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        @sort-change="sortChange"
        :data="baselines"
        ref="table"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" align="center"></el-table-column>
        <el-table-column prop="versionNo" label="版本" align="center">
          <template slot-scope="scope">
            V{{scope.row.versionNo}}
          </template>
        </el-table-column>
        <el-table-column prop="creator.realname" label="发布者"></el-table-column>
        <el-table-column prop="createTime" label="发布日期" width="160"></el-table-column>
        <!-- <el-table-column prop="stateText" label="状态">
          <template slot-scope="scope">
            <span v-if="scope.row.state != 4">{{scope.row.stateText}}</span>
            <el-tooltip v-else-if="scope.row.auditReason" placement="top">
              <span slot="content" v-html="scope.row.auditReason"></span>
              <el-link type="danger">{{scope.row.stateText}}</el-link>
            </el-tooltip>
            <el-link type="danger" v-else>{{scope.row.stateText}}</el-link>
          </template>
        </el-table-column> -->
        <el-table-column label="操作" width="300px">
          <template slot-scope="scope">
            <el-button type="text" @click="checkBaseline(scope.row)" >文件列表</el-button> 
            <el-button type="text" @click="test(scope.row)">仿真测试</el-button>
            <el-button type="text" @click="del(scope.row)">废除版本</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        :page-size="10"
        @current-change="toPage">
      </el-pagination>
    </el-dialog>
    <assets-dialog
      :isAdd="addBaselineShow"
      :baseline="baselineAssets"
      :active.sync="showAssetsDialog"
      @test="test"
      @reload="reload">
    </assets-dialog>
    <test-dialog :active.sync="showTestDialog"></test-dialog>
    <baseline-diff :show.sync="showDiffDialog" :diff-data="diffData"></baseline-diff>
    <baseline-audit :active.sync="showAuditDialog" :auditData="auditData" @reload="reload"></baseline-audit>
  </div>
</template>

<script>
import assetsDialog from "./assets-dialog";
import testDialog from "./test-dialog";
import baselineDiff from "./baseline-diff";
import baselineAudit from "./baseline-audit";
export default {
  name: "index",
  components: {
    "assets-dialog": assetsDialog,
    "test-dialog": testDialog,
    baselineDiff,
    baselineAudit
  },
  props: {
    active: Boolean,
    baselineData: Object
  },
  data() {
    return {
      tableLoading: false,
      showAssetsDialog: false,
      showTestDialog: false,
      showDiffDialog: false,
      showAuditDialog: false,
      baselines: [],
      tablePage: 1,
      total: 0,
      sortOption: {
        sortField: "",
        sortDirection: ""
      },
      testLoading: false,
      saveLoading: false,
      show: this.active,
      addBaselineShow: false,
      baselineAssets: null,
      diffData: {
        uuid: '',
        name: '',
        baselines: []
      },
      tabSelect: [],
      auditData:{},
    };
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    },
    baselineData: function(newVal) {
      this.baselineData = newVal;
    }
  },
  mounted() {
    this.initData();
  },
  methods: {
    closed() {
      this.assets = [];
      this.$emit("update:active", false);
      this.$emit('closed')
    },
    reload(){
      this.initData()
    },
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    initData() {
      this.tableLoading = true;
      this.$axios.get("/baseline", {
        params: {
          page: this.tablePage,
          packageUuid: this.baselineData.uuid
        }
      }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          res.data.data.forEach(e => {
            if (e.createTime) {
              e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
              // state
              switch (e.state) {
                case 0:
                  e.stateText = "待发布";
                  break;
                case 1:
                  e.stateText = "待审核";
                  break;
                case 2:
                  e.stateText = "待生效";
                  break;
                case 3:
                  e.stateText = "已生效";
                  break;
                case 4:
                  e.stateText = "已拒绝";
                  break;
                default:
                  e.stateText = "未知";
              }
            }
            if (e.auditReason) {
              e.auditReason = e.auditReason.replace(/\n/g, '<br>')
            } else {
              e.auditReason = ''
            }
          });
          this.baselines = res.data.data;
          this.total = res.data.total;
        }
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    openDiff() {
      if (this.tabSelect.length == 2) {
        this.diffData.uuid = this.baselineData.uuid;
        this.diffData.name = this.baselineData.name;
        this.diffData.baselines = this.tabSelect.map(e => e.versionNo)
        this.showDiffDialog = true;
      } else {
        this.$message.warning( "请勾选两个版本对比")
      }
    },
    moreOperation({ row, op }) {
      if (op == "download") {
        this.download(row);
      } else if (op == "del") {
        this.del(row);
      } else if (op == "check") {
        this.checkBaseline(row);
      }
    },
    test(row) {
      this.showTestDialog = true;
      this.baselineData.baselineVersion = row.versionNo;
      this.$store.commit("setPkg", this.baselineData);
    },
    del(row) {
      this.$confirm("此操作将永久废除该版本, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$axios.post("/baseline/delete", this.$qs.stringify({
          uuid: row.uuid
        })).then(res => {
          this.loading = false;
          if (res.data.code == 0) {
            this.$message.success("删除成功")
            this.initData();
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "删除失败")
          }
        })
        .catch(() => {
          this.$message.error("删除失败")
          this.loading = false;
        });
      });
    },
    addBaseline() {
      this.$store.commit("setPkg", this.baselineData);
      this.showAssetsDialog = true;
      this.addBaselineShow = true;
    },
    checkBaseline(data) {
      this.$store.commit("setPkg", this.baselineData);
      this.showAssetsDialog = true;
      this.addBaselineShow = false;
      this.baselineAssets = data;
    },
    sortChange(data) {
      this.sortOption.sortField = data.prop;
      this.sortOption.sortDirection =
        data.order == "ascending" ? "asc" : "desc";
      this.initData();
    },
    download(row) {
      const that = this;
      that.tableLoading = true
      this.$axios({
        method: "get",
        url: "/knowledgepackage/download?uuid=" + that.baselineData.uuid + "&baselineVersion=" + row.versionNo,
        responseType: "blob"
      }).then(res => {
        that.tableLoading = false
        if (res.data.type == "application/json") {
          const reader = new FileReader();
          reader.onload = function() {
            const json = JSON.parse(reader.result);
            if (json.code == 401) {
              localStorage.removeItem('hasLogin')
              const from = that.$router.currentRoute.fullPath
              that.$router.push('/login?from=' + from)
            } else {
              that.$message.error(json.msg ? json.msg : "下载失败");
            }
          };
          reader.readAsText(res.data);
        } else {
          let url = window.URL.createObjectURL(new Blob([res.data]));
          let a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          a.target = "_blank";
          a.setAttribute("download", that.baselineData.name + '_V' + row.versionNo + '.baseline');
          document.body.appendChild(a);
          a.click();
        }
        that.tableLoading = false;
      }).catch(() => {
        that.$message.error("下载失败");
        that.tableLoading = false;
      });
    },
    genMicro(row) {
      this.tableLoading = true;
      this.$axios
        .post(
          "/baseline/publish",
          this.$qs.stringify({
            baselineVersion: row.versionNo,
            packageUuid: this.baselineData.uuid
          })
        )
        .then(res => {
          if (res.data.code == 0) {
            this.$message.success("发布成功");
            this.initData();
          } else {
            this.$message.error(res.data.msg ? res.data.msg : "操作失败");
          }
        })
        .catch(() => {
          this.$message.error("操作失败");
        })
        .then(() => {
          this.tableLoading = false;
        });
    },
    //审核
    auditMicro(row) {
      this.auditData = row;
      this.auditData.packageUuid = this.baselineData.uuid
      this.showAuditDialog = true;
    },
    //选中对比
    handleSelectionChange(data) {
      this.tabSelect = [];
      data.forEach(item => {
        this.tabSelect.push(item);
      });
    },
    showMoreOperation(row) {
      return this.showDownloadOperation(row) || this.showDelOperation(row)
    },
    showDownloadOperation(row) {
      if (!this.$hasPerm('project.pkg.baseline:download')) {
        return false
      }
      if (row.state != 2 && row.state != 3) {
        // 基线状态不是待生效或已生效
        return false
      }
      return true
    },
    showDelOperation(row) {
      if (!this.$hasPerm('project.pkg.baseline:del')) {
        return false
      }
      if (row.state == 3) {
        // 基线状态是已生效
        return false
      }
      return true
    }
  }
};
</script>

<style scoped>
.note {
  margin: 0 0 20px;
  padding: 10px 30px 10px 15px;
  /*border-left: 5px solid #eee;*/
  width: 98%;
}

.note-success {
  background-color: transparent;
  /*border-left: 5px solid #fff !important;*/
  /*border-bottom:1px solid #fff;border-radius: 2px;*/
}

p.block {
  margin: 5px 0;
  color: #fff;
}
.auditReason {
  cursor: pointer;
}
</style>
