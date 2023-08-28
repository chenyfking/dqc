<template>
  <el-dialog
    title="用户操作日志"
    width="60%"
    :visible="show"
    @update:visible="close"
    @open="open"
    @closed="closed" :append-to-body="true">
     <user-logs :user-uuid="userUuid"></user-logs>
  </el-dialog>
</template>

<script>
  import UserLogs from "../user/user-logs";
  export default {
    components: {
      UserLogs
    },
    name: "user-logs-dialog",
    props: {
      show:Boolean,
      userUuid: String
    },
    data() {
      return {

      }
    },
    mounted() {
     
    },
    watch: {
      userUuid(newval,oldVal){
        this.$nextTick(() => {
          this.userUuid= newval;
        });
      }
    },
    methods: {
      open() {
      },
      close() {
        this.$emit('update:show', false)
      },
      closed() {
      }
    }
  }
  
</script>

<style>
</style>