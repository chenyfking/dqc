<template>
  <div class="treeMenu">
    <!-- 有菜单的节点 -->
    <div class="searchModel" v-if="isHaveMenu(menuData.type)">
      <span>
        <i v-if="menuData.locked" class="el-icon-lock"></i>
        <i v-else-if="menuData.editor" class="el-icon-edit"></i>
        <i v-if="menuData.type == 'menu-fact'" :class="menuData.icon"></i>
        <i v-else :class="!menuData.leaf?'el-icon-folder-opened':menuData.icon"></i>
        <span>{{ menuData.label }}</span>
      </span>
      <!-- 数据结构上传JAR -->
      <span v-if="menuData.type=='menu-fact' && $hasPerm('project.asset:add')">
        <!-- <dropdown trigger="hover">
          <span style="color:#fff;">
            <i class="el-icon-more"></i>
          </span>
          <dropdown-menu slot="dropdown">
            <dropdown-item>
              <el-upload
                action
                :on-change="upload"
                :auto-upload="false"
                :show-file-list="false"
                accept=".jar, .class, .java">
                <i class="iconfont iconVersionupdaterule-copy"></i> 上传文件
              </el-upload>
            </dropdown-item>
          </dropdown-menu>
        </dropdown> -->
      </span>
      <!-- 文件夹操作 -->
      <span v-else-if="!menuData.leaf">
        <dropdown @command="handleDircom" trigger="hover">
          <span style="color:#fff;">
            <i class="el-icon-more"></i>
          </span>
          <dropdown-menu slot="dropdown">
            <dropdown-item
              v-if="$hasPerm('project.asset:edit')"
              icon="el-icon-edit"
              :command="beforeHandleCommand('edit',menuData)">
              编辑
            </dropdown-item>
            <!-- <dropdown-item
              v-if="$hasPerm('project.asset:lock')"
              :icon="menuData.locked ? 'el-icon-unlock' : 'el-icon-lock'"
              :command="beforeHandleCommand('lock',menuData)">
              {{menuData.locked ? '解锁' : '锁定'}}
            </dropdown-item> -->
            <dropdown-item
              v-if="$hasPerm('project.asset:del')"
              icon="el-icon-delete"
              :command="beforeHandleCommand('del',menuData)">
              删除
            </dropdown-item>
          </dropdown-menu>
        </dropdown>
      </span>
      <!-- 文件操作 -->
      <span v-else-if="menuData.leaf">
        <dropdown @command="handleFilecom" trigger="hover">
          <span style="color:#fff;">
            <i class="el-icon-more"></i>
          </span>
          <dropdown-menu slot="dropdown">
            <!-- 非模板文件节点、非测试案例节点 -->
            <template v-if="!menuData.type.startsWith('tpl_') && menuData.type != 'testcase' && $hasPerm('project.asset:edit')">
              <dropdown-item
                icon="el-icon-edit"
                :command="beforeHandleCommand('move',menuData)">
                移动
              </dropdown-item>
              <dropdown-item
                icon="iconfont iconlist-2-copy"
                :command="beforeHandleCommand('copy',menuData)">
                复制
              </dropdown-item>
            </template>
            <!-- 非模板文件节点 -->
            <dropdown-item
              v-if="!menuData.type.startsWith('tpl_') && $hasPerm('project.asset:lock')"
              :icon="menuData.locked || menuData.editor ? 'el-icon-unlock' : 'el-icon-lock'"
              :command="beforeHandleCommand('lock',menuData)">
              {{menuData.locked || menuData.editor ? '解锁' : '锁定'}}
            </dropdown-item>
            <dropdown-item
              v-if="$hasPerm('project.asset:del')"
              icon="el-icon-delete"
              :command="beforeHandleCommand('del', menuData)">
              删除
            </dropdown-item>
          </dropdown-menu>
        </dropdown>
      </span>
    </div>
    <!-- 没菜单的节点 -->
    <div v-else>
      <span>
        <i v-if="menuData.locked" class="el-icon-lock"></i>
        <i v-if="menuData.icon" :class="menuData.icon"></i>
        <i v-else :class="!menuData.leaf?'el-icon-folder-opened':menuData.icon"></i>
        <span>{{ menuData.label }}</span>
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: "index",
  props: {
    menuNode: Object,
    menuData: Object
  },
  watch: {},
  data() {
    return {
      haveMenu: [
        "menu-fact",
        "fact",
        "constant",
        "guidedrule",
        'tpl_guidedrule',
        'testcase'
      ]
    };
  },
  methods: {
    handleDircom(command) {
      this.$emit("handleCommand", "DIR", command);
    },
    isHaveMenu(type) {
      return this.haveMenu.indexOf(type) != -1
    },
    handleFilecom(command) {
      this.$emit("handleCommand", "FILE", command);
    },
    beforeHandleCommand(item, data) {
      return {
        command: item,
        data: data
      };
    },
    upload(e) {
      this.$emit("importFact", e);
    }
  }
};
</script>

<style>
.treeMenu {
  width: 100%;
}
.searchModel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
