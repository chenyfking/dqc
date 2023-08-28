<template>
  <div class="addAssets">
    <el-dialog
      title="添加规则文件"
      :size="$dialogTop"
      :visible.sync="show"
      width="60%"
      append-to-body
      @closed="closed"
      @open="open">
      <div class="searchContent">
        <el-input
          placeholder="搜索"
          style="width:220px; margin-right: 5px;"
          v-model="name"
          class="search"
          @keyup.enter.native="search">
          <i slot="suffix" class="el-input__icon el-icon-search" @click="search()"></i>
        </el-input>
        <el-button @click="show = false">关 闭</el-button>
      </div>

      <el-tabs type="border-card" v-loading="loading" element-loading-text="数据加载中...">
        <el-tab-pane v-for="ag in assetsGroup" :key="ag.type" :label="ag.type">
          <el-table
            class="tb-edit"
            :data="ag.list"
            style="height:300px; overflow-y: auto;"
            highlight-current-row
            @cell-click="cellClick">
            <el-table-column label="文件名称" prop="name"></el-table-column>
            <el-table-column label="版本号" align="center">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.select"
                  :ref="'input_original_' + scope.row.uuid"
                  placeholder="请选择"
                  class="scopSelect"
                  @change="selectVerion(scope.row)">
                  <el-option
                    :value="item.versionNo"
                    :label="'V'+item.versionNo"
                    v-for="(item,index) in scope.row.assetsVersions"
                    :key="index">
                  </el-option>
                </el-select>
                <span class="originSelect">{{'V'+scope.row.select}}</span>
              </template>
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
              <template slot-scope="scope">
                {{$moment(scope.row.createTime).format('YYYY-MM-DD HH:mm')}}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button @click.stop="viewAssets(scope.row)" type="text">查看</el-button>
                <el-button @click.stop="add(scope.row, ag.type, scope.$index, ag.list)" type="text">添加</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show">
    </assets-view>
  </div>
</template>

<script>
export default {
  name: "assets-add-dialog",
  props: {
    active: Boolean,
    assets: {
      type: Array,
      default: function() {
        return []
      }
    },
    filterExists: {
      type: Boolean,
      default: true
    }
  },
  watch: {
    active: function(newVal) {
      this.show = newVal;
    }
  },
  data() {
    return {
      show: this.active,
      loading: false,
      assetsGroup: [],
      name: "",
      checkList: [],
      sName: "",
      sDirName: "",
      viewingAssets: {
        uuid: "",
        version: 0,
        show: false
      }
    };
  },
  methods: {
    closed() {
      this.$emit("update:active", false);
      this.$emit("closed");
      this.name = "";
      this.checkList = [];
    },
    open() {
      this.loading = true;
      this.$axios.get("/assets/query", {
        params: {
          projectUuid: this.$route.params.uuid,
          name: this.name
        }
      }).then(res => {
        if (res.data.code == 0) {
          this.assetsGroup = [];
          res.data.data.forEach(e => {
            if (e.type != 'guidedrule' ) {
              return;
            }
            e.list.forEach(item => {
              item.select = item.assetsVersions[0].versionNo;
              item.versionDes = item.assetsVersions[0].versionDes;
              item.createTime = item.assetsVersions[0].createTime;
            });
            // 过滤掉已经添加的规则文件
            if (this.filterExists) {
              e.list = e.list.filter(f => {
                let exists = false
                this.assets.some(r => {
                  if (f.uuid == r.uuid) {
                    exists = true
                    return true
                  }
                })
                return !exists
              })
            }
            e.type = this.$utils.getAssetsTypeText(e.type);
            this.assetsGroup.push(e);
          });
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '查询失败')
        }
        this.loading = false;
      }).catch((e) => {
        this.loading = false;
        console.log(e)
        this.$message.error('查询失败')
      });
    },
    viewAssets(row) {
      this.viewingAssets.uuid = row.uuid;
      this.viewingAssets.version = row.select;
      this.viewingAssets.show = true;
    },
    selectVerion(row) {
      row.assetsVersions.forEach(item => {
        if (row.select == item.versionNo) {
          row.versionDes = item.versionDes;
          row.createTime = item.createTime;
        }
      });
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.uuid][0];
      if (column.label == "版本号" && el) {
        this.$nextTick(function() {
          el.toggleMenu();
        });
      }
    },
    search() {
      this.open();
    },
    add(row, type, i, list) {
      this.$emit('add', row, type)
      if (this.filterExists) {
        list.splice(i, 1)
      }
    }
  }
};
</script>

<style scoped>
.searchContent {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}
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
.addFile {
  font-size: 16px;
  margin-bottom: 6px;
}
.addFile .title {
  color: #909399;
}
.addFile .selectAsset {
  cursor: pointer;
  color: #409eff;
}
.testcase .activeClass {
  color: #f56c6c;
}
.originSelect {
  cursor: pointer;
  color: #409eff;
}
.scopSelect {
  width: 120px;
}
.el-table::before {
  height: 0;
}
.originSelect {
  cursor: pointer;
}
</style>
