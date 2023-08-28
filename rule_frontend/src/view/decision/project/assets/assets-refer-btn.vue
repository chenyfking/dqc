<template>
  <div class="referContent">
    <el-button
      icon="el-icon-search"
      @click="initData"
      :loading="loading"
      v-if="isButtonModel">
      {{loading ? '查询中...' : '查询引用'}}
    </el-button>

    <el-dialog
      title="引用结果"
      :visible.sync="show"
      :close-on-click-modal="false"
      class="referContent"
      v-if="show"
      append-to-body
      width="80%"
      @closed="closeRefer">
      <el-tabs type="border-card" v-if="assetsGroup.length">
        <el-tab-pane
          v-for="ag in assetsGroup"
          :key="ag.type"
          :label="$utils.getAssetsTypeText(ag.type)">
          <el-table
            class="referTable tb-edit"
            v-loading="tableLoading"
            element-loading-text="数据加载中..."
            :data="ag.list"
            highlight-current-row
            @cell-click="cellClick">
            <el-table-column label="文件名称" show-overflow-tooltip>
              <template slot-scope="scope">
                <el-button
                  v-if="ag.type !== 'pkg' && ag.type !== 'fact' && ag.type !== 'constant'"
                  @click.stop="viewAssets(scope.row)"
                  type="text">
                  {{scope.row.name}}
                </el-button>
                <span v-else>{{scope.row.name}}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="type"
              label="类型"
              v-if="assets.type == 'fact' || assets.type == 'constant'">
              <template slot-scope="scope">
                <span>{{scope.row.template ? '模板' : '规则'}}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="ag.type == 'pkg' ? '知识包版本' : '版本'"
              align="center"
              style="width: 60%;">
              <template slot-scope="scope" v-if="scope.row.select || scope.row.select == 0">
                <el-select
                  v-model="scope.row.select"
                  :ref="'input_original_' + scope.row.uuid"
                  placeholder="请选择"
                  class="scopSelect"
                  @change="selectVerion(scope.row)"
                  v-if="scope.row.assetsVersions.length">
                  <el-option
                    :value="item.versionNo"
                    :label="item.versionNo == 0 ? '正在编辑' : 'V' + item.versionNo"
                    v-for="(item,index) in scope.row.assetsVersions"
                    :key="index"
                  ></el-option>
                </el-select>
                <span class="originSelect" v-if="scope.row.assetsVersions.length">
                  {{scope.row.select == 0 ? '正在编辑' : 'V' + scope.row.select}}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="描述" show-overflow-tooltip>
              <template slot-scope="scope">
                {{scope.row.versionDes}}
               </template>
            </el-table-column>
            <el-table-column label="状态">
              <template slot-scope="scope">
                <span>{{scope.row.recycle ? '被删除' : '正常'}}</span>
              </template>
            </el-table-column>
            <el-table-column label="创建时间">
              <template slot-scope="scope">
                {{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-tabs type="border-card" v-if="!assetsGroup.length">
        该文件没有被引用
      </el-tabs>

      <div slot="footer">
        <el-button @click="closeRefer" icon="el-icon-error">关 闭</el-button>
      </div>
    </el-dialog>
    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show"
      :template="viewingAssets.template">
    </assets-view>
  </div>
</template>

<script>
export default {
  name: "refer-btn",
  props: {
    assets: Object,
    isButtonModel: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      loading: false,
      show: false,
      tableLoading: false,
      searchList: [],
      assetsGroup: [],
      tablePage: 1,
      total: 0,
      viewingAssets: {
        uuid: "",
        version: 0,
        show: false,
        template: false
      },
    };
  },
  mounted() {
    if (!this.isButtonModel) {
      this.show = true
      this.initData()
    }
  },
  methods: {
    initData() {
      this.loading = true;
      this.$axios.get("/assets/reference", {
        params: {
          assetsUuid: this.assets.uuid,
          type: this.assets.type,
          versionNo: this.assets.versionNo ? this.assets.versionNo : ''
        },
      }).then((res) => {
        this.loading = false;
        if (res.data.code == 0) {
          this.assetsGroup = [];
          res.data.data.forEach((e) => {
            e.list.forEach((item) => {
              if (item.assetsVersions) {
                item.select = item.assetsVersions[0].versionNo;
                item.versionNo = item.assetsVersions[0].versionNo;
                item.versionDes = item.assetsVersions[0].versionDes;
                item.createTime = item.assetsVersions[0].createTime;
              } else {
                item.select = "";
                item.versionDes = item.description?item.description :'';
                item.createTime = item.createTime?item.createTime :'';
                item.assetsVersions = []
              }
            });
            this.assetsGroup.push(e)
          });
          this.show = true;
        } else {
          this.$message.error("查询失败");
        }
      }).catch(() => {
        this.$message.error("查询失败");
        this.loading = false;
      });
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.uuid][0];
      if (column.label == "版本号" && el) {
        this.$nextTick(function () {
          el.toggleMenu();
        });
      }
    },
    selectVerion(row) {
      row.assetsVersions.forEach((item) => {
        if (row.select == item.versionNo) {
          row.versionNo = item.versionNo;
          row.versionDes = item.versionDes;
          row.createTime = item.createTime;
        }
      });
    },
    handleClose() {
      this.searchList = [];
    },
    toPage(page) {
      this.tablePage = page;
      this.initData();
    },
    viewAssets(row) {
      this.viewingAssets.uuid = row.uuid;
      this.viewingAssets.version = row.versionNo;
      this.viewingAssets.template = row.template;
      this.viewingAssets.show = true;
    },
    closeRefer() {
      if (!this.isButtonModel) {
        this.$emit('close')
      }
      this.show = false
    }
  },
};
</script>

<style scoped>
.tb-edit .el-input,
.tb-edit .el-input-number,
.tb-edit .el-select,
.tb-edit .el-dropdown {
  display: none;
}

.tb-edit .current-row .el-input,
.tb-edit .current-row .el-input-number,
.tb-edit .current-row .el-select,
.tb-edit .current-row .el-select + .el-dropdown {
  display: inline-block;
}

.tb-edit .current-row .el-input + span,
.tb-edit .current-row .el-input-number + span,
.tb-edit .current-row .el-select + span,
.tb-edit .current-row .el-dropdown + span {
  display: none;
}
.originSelect {
  cursor: pointer;
  color: #409eff;
}
</style>
