<template>
  <div class="ledger">
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>财务台账</span>
          <el-tag type="success">总金额: ¥{{ totalAmount }}</el-tag>
        </div>
      </template>
      <el-table :data="ledgerList" border stripe v-loading="loading">
        <el-table-column prop="id" label="台账编号" width="250" />
        <el-table-column prop="reimbursementId" label="报销单号" width="250" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="expenseType" label="费用类型" width="120">
          <template #default="scope">
            {{ getExpenseTypeName(scope.row.expenseType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="scope">
            <span style="font-weight: bold; color: #f56c6c">¥{{ formatAmount(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceNumber" label="发票编号" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="ledgerList.length === 0" description="暂无财务台账记录" :image-size="100" style="margin-top: 20px" />
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Ledger',
  data() {
    return {
      ledgerList: [],
      loading: false
    }
  },
  computed: {
    totalAmount() {
      return this.ledgerList.reduce((sum, item) => sum + parseFloat(item.amount || 0), 0).toFixed(2)
    }
  },
  mounted() {
    this.loadLedgers()
  },
  activated() {
    this.loadLedgers()
  },
  watch: {
    $route(to) {
      if (to.path === '/ledger') {
        this.loadLedgers()
      }
    }
  },
  methods: {
    async loadLedgers() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/ledgers')
        this.ledgerList = response.data
      } catch (error) {
        console.error('加载财务台账失败', error)
        this.$message.error('加载财务台账失败')
      } finally {
        this.loading = false
      }
    },
    getExpenseTypeName(type) {
      const names = {
        TRANSPORTATION: '交通费',
        ACCOMMODATION: '住宿费',
        MEAL: '餐饮费',
        OFFICE_SUPPLY: '办公用品',
        TRAVEL: '差旅费',
        OTHER: '其他'
      }
      return names[type] || type
    },
    formatAmount(amount) {
      if (amount === null || amount === undefined || amount === '') return '0.00'
      return parseFloat(amount).toFixed(2)
    }
  }
}
</script>

<style scoped>
.ledger {
  padding: 0;
}
</style>
