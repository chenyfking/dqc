<template>
  <div class="projectContent">
    <div class="toolbar">
      <div>
        <el-input
          placeholder="搜索"
          v-model.trim="projectSearchName"
          class="search"
          @keyup.enter.native="search">
            <i slot="suffix" class="el-input__icon el-icon-search" @click="search"></i>
            </el-input>
        </div>
      </div>
      <el-table
        v-loading="tableLoading"
        element-loading-text="数据加载中..."
        :data="projects"
        @sort-change="sortChange"
        @row-dblclick="(row) => $router.push(`/decision/project/${row.uuid}`)">
        <el-table-column label="名称" width="350">
          <template slot-scope="scope">
            <el-button
              type="text"
              @click.stop="openProject(scope.row.uuid)"
            >{{scope.row.name}}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column sortable="custom" prop="updateTime" label="更新时间" width="150"></el-table-column>
        <el-table-column sortable="custom" prop="createTime" label="创建时间" width="150"></el-table-column>
        <!-- <el-table-column prop="creator.realname" label="创建人" width="80" show-overflow-tooltip></el-table-column> -->
         <el-table-column label="操作" width="130">
          <template slot-scope="scope">
            <el-button
              type="text"
              @click.stop="openPkg(scope.row.uuid)">
              知识包
            </el-button>
          </template>
         </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        @current-change="toPage"
      >
    </el-pagination>
  </div>
</template>

<script>
export default {
  name: "user-project",
  props: {
    userUuid: String
  },

  data() {
    return {
      projects: [],
      total: 0,
      page: 1,
      form: {
        uuid: "",
        userUuid: "",
        name: "",
        description: "",
      },
      sortOption: {
        sortField: "",
        sortDirection: "",
      },
      projectSearchName: "",
      tableLoading: false
    };
  },
  mounted() {
    this.loadData();
  },
  watch: {
    userUuid(){
      this.loadData()
    }
  },
  methods: {
    sortChange(data) {
      this.sortOption.sortField = data.prop;
      this.sortOption.sortDirection =
        data.order == "ascending" ? "asc" : "desc";
      this.loadData();
    },
    search() {
      this.page = 1
      this.loadData()
    },
    loadData() {
      this.tableLoading = true;
      this.$axios.get("/user/project", {
        params: {
          page: this.page,
          projectName: this.projectSearchName,
          sortField: this.sortOption.sortField,
          sortDirection: this.sortOption.sortDirection,
          userUuid: this.userUuid
        }
      }).then(res => {
        if (res.data.code == 0) {
          res.data.data.forEach((e) => {
            if (e.createTime) {
              e.createTime = this.$moment(e.createTime).format("YYYY-MM-DD HH:mm");
            }
            if (e.updateTime) {
              e.updateTime = this.$moment(e.updateTime).format("YYYY-MM-DD HH:mm");
            }
            if (e.creator && !e.creator.realname) {
              e.creator.realname = e.creator.username
            }
          });
          this.projects = res.data.data;
          this.total = res.data.total;
        }
        this.tableLoading = false;
      }).catch(() => {
        this.tableLoading = false;
      });
    },
    toPage(p) {
      this.page = p;
      this.loadData();
    },
    openProject(uuid){
      let routeUrl = this.$router.resolve({
          path: "/decision/project/" + uuid
      });
      window.open(routeUrl.href);
    },
    openPkg(uuid){
      let routeUrl = this.$router.resolve({
          path: "/decision/project/" + uuid +"/knowledgepackage"
      });
      window.open(routeUrl.href, '_blank');
    }
  },
};
</script>

<style scoped>
</style>
