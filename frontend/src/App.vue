<template>
  <div id="app">
    <el-container style="height: 100vh">
      <el-header style="background: #001529; color: white; display: flex; align-items: center; padding: 0 20px">
        <h1 style="margin: 0; font-size: 20px">企业报销审批系统</h1>
        <div style="margin-left: auto; display: flex; gap: 20px">
          <span>当前用户：{{ currentUser?.name || '未登录' }}</span>
          <el-select v-model="currentUserId" placeholder="选择用户" style="width: 150px" @change="switchUser">
            <el-option
              v-for="employee in employees"
              :key="employee.id"
              :label="employee.name + ' (' + employee.department + ')'"
              :value="employee.id"
            />
          </el-select>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" style="background: #001529">
          <el-menu
            :router="true"
            :default-active="$route.path"
            background-color="#001529"
            text-color="#ffffff"
            active-text-color="#1890ff"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/create">提交报销</el-menu-item>
            <el-menu-item index="/my">我的报销</el-menu-item>
            <el-menu-item index="/approval">待我审批</el-menu-item>
            <el-menu-item index="/payment">打款记录</el-menu-item>
            <el-menu-item index="/ledger">财务台账</el-menu-item>
            <el-menu-item index="/risk">风险预警</el-menu-item>
            <el-menu-item index="/stats">统计分析</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main style="background: #f0f2f5; padding: 20px">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      employees: [],
      currentUserId: 'E001',
      currentUser: null
    }
  },
  mounted() {
    this.loadEmployees()
  },
  methods: {
    async loadEmployees() {
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/employees')
        this.employees = response.data
        this.switchUser(this.currentUserId)
      } catch (error) {
        console.error('加载员工列表失败', error)
      }
    },
    switchUser(userId) {
      this.currentUser = this.employees.find(e => e.id === userId)
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
