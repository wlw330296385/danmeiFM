const Sqllite = function(name) {
	this.name = name ? name : 'default';
	
	/**
	 * 执行查询的SQL语句
	 */
	this.selectSql = function(sql, success, fail) {
		plus.sqlite.selectSql({
			name: this.name,
			sql: sql,
			success,
			fail
		});
	};

	/**
	 * 执行增删改等操作的SQL语句
	 */
	this.executeSql = function(sql, success, fail) {
		plus.sqlite.executeSql({
			name: this.name,
			sql: sql,
			success,
			fail
		});
	};

	/**
	 * 执行事务
	 */
	this.transaction = function(operation) {
		plus.sqlite.transaction({
			name: this.name,
			operation: operation ? operation : 'begin',
			success: function(e) {
				console.log('transaction success!');
			},
			fail: function(e) {
				console.log('transaction failed!');
			}
		});
	};

	/**
	 * 关闭数据库
	 */
	this.closeDatabase = function() {
		plus.sqlite.closeDatabase({
			name: this.name,
			success: function(e) {
				console.log('closeDatabase success!');
			},
			fail: function(e) {
				console.log('closeDatabase failed!');
			}
		});
	};

	/**
	 * 判断数据库是否打开
	 */
	this.isOpenDatabase = function() {
		return plus.sqlite.isOpenDatabase({
			name: this.name,
			path: '_doc/data.db'
		});
	};

	/**
	 * 打开数据库
	 */
	this.openDatabase = function() {
		if (this.name && !this.isOpenDatabase()) {
			plus.sqlite.openDatabase({
				name: this.name,
				path: '_doc/data.db',
				success: function(e) {
					console.log('openDatabase success!');
				},
				fail: function(e) {
					console.log('openDatabase failed!');
				}
			});
		}
	};
	
	this.openDatabase();
};

export default Sqllite;
