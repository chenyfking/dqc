<template>
  <div style="overflow-x: auto;">
    <div v-for="(e, i) in value.actions" :key="getKey(i)" style="white-space: nowrap;">
      <action v-model="value.actions[i]" :loop-target="loopTarget" :readonly="readonly"></action>
      <template v-if="!readonly">
        <i class="el-icon-delete icon-btn-mini" @click="delAction(i)"></i>
        <i class="el-icon-document-copy icon-btn-mini" @click="copyAction(i)"></i>
      </template>
    </div>
    <el-tooltip content="添加动作" v-if="!readonly">
      <dropdown @command="addAction">
        <i class="el-icon-circle-plus-outline" style="cursor: pointer"></i>
        <dropdown-menu slot="dropdown">
          <dropdown-item command="ASSIGN">赋值</dropdown-item>
          <dropdown-item command="FUNC">执行函数</dropdown-item>
          <!-- <dropdown-item command="THIRDAPI">调用接口</dropdown-item> -->
          <dropdown-item command="MODEL">调用模型</dropdown-item>
        </dropdown-menu>
      </dropdown>
    </el-tooltip>
  </div>
</template>

<script>
  export default {
    name: "rhs",
    props: {
      value: {
        type: Object,
        default: function() {
          const defaultValue = {
            actions: []
          }
          this.$emit('input', defaultValue)
          return defaultValue
        }
      },
      readonly: {
        type: Boolean,
        default: false
      },
      loopTarget: Object
    },
    methods: {
      addAction(type) {
        this.value.actions.push({type})
      },
      delAction(index) {
        this.value.actions.splice(index, 1)
      },
      copyAction(index) {
        const copy = this.$utils.copy(this.value.actions[index])
        copy._id = this.$utils.randomCode(4)
        this.value.actions.splice(index, 0, copy)
      },
      getKey(i) {
        let action = this.value.actions[i]
        if (!action._id) action._id = this.$utils.randomCode(4)
        return action._id
      },
      click(e) {
        console.log(e)
      },
      command(e) {
        console.log(e)
      },
      visibleChange(e) {
        console.log(e)
      }
    }
  }
</script>

<style scoped>

</style>
