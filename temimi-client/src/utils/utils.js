import EmojiList from '@/assets/json/emoji.json';


/**
 * 将文本中的URL或其他可识别的链接转换为可点击的超链接
 * @param {String} text 需要识别的文本
 * @returns {String} 带有超链接a标签的文本
 */
export function linkify(text) {
    if (text) {
        // 匹配URL的正则表达式
        var urlRegex = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#/%=~_|$?!:,.]*[A-Z0-9+&@#/%=~_|$])/gi;

        // 将匹配到的URL替换为带有链接的HTML
        var linkedText = text.replace(urlRegex, function (url) {
            return '<a href="' + url + '" class="v-url" target="_blank">' + url + '</a>';
        });

        return linkedText;
    } else {
        return text; // 如果 text 未定义，则返回原始值
    }
}


/**
 * 将文本中的有效表情转换成图片
 * @param {String} text 需要识别的文本
 * @returns {String} 带有表情的html文本
 */
export function emojiText(text) {
    if (text) {
        // 匹配 [表情] 格式的字符串
        const regex = /\[(.*?)\]/g;
        const replacedText = text.replace(regex, (match, p1) => {
            // 查找匹配的表情名称在 EmojiList 中的索引
            const emojiIndex = EmojiList.findIndex(emoji => emoji.name === `[${p1}]`);

            // 如果找到了对应的表情
            if (emojiIndex !== -1) {
                const { name, url } = EmojiList[emojiIndex];
                // 构建替换的 HTML
                return `<a class="emotion-items" title="${name}"><div class="img-emoji" style="background-image:url('${url}'); height:20px; width:20px;"></div></a>`;
            } else {
                // 如果未找到对应的表情，返回原始字符串
                return match;
            }
        });

        return replacedText;
    } else {
        return text;
    }
}


/**
 * 对文本进行关键词高亮
 * @param {String} keyword 要高亮的关键词
 * @param {String} inputString 要转换的文本
 */
export function highlightKeyword(keyword, inputString) {
    // 使用正则表达式创建一个匹配关键词的模式
    const regex = new RegExp(`(${keyword.split('').join('|')})`, 'gi');
    // 使用字符串替换函数进行替换，添加高亮标签
    return inputString.replace(regex, '<em class="suggest_high_light">$1</em>');
}


/**
 * 处理播放时长，将秒数转换为分钟和秒的格式
 * @param {Number} time 传入的总时长，以秒为单位
 * @returns {String} 处理后的时间字符串，格式为 'mm:ss'
 */
export function handleTime(time) {
    time = parseInt(time);
    time = Math.floor(time);
    let m = Math.floor(time / 60);
    let s = Math.floor(time % 60);
    m = m < 10 ? '0' + m : m;
    s = s < 10 ? '0' + s : s;
    return m + ':' + s;
}


/**
 * 将格式化后的播放时长还原为秒数
 * @param {String} time 格式为 'mm:ss' 的时间字符串
 * @returns {Number} 总时长秒数
 */
export function returnSecond(time) {
    time = time.split(':');
    let m = parseInt(time[0]);
    let s = parseInt(time[1]);
    return m * 60 + s;
}


/**
 * 处理大于1万的数字
 * @param {Number} num 如：198765
 * @returns {String} 转换后：'19.9万'
 */
export function handleNum(num) {
    if (num > 10000) {
        num = (num / 10000).toFixed(1);
        return num + '万';
    } else {
        return num;
    }
}


/**
 * 统一的日期时间格式化函数
 *
 * @param {Number|String|Date|null|undefined} dateTime - 传入的日期时间,可以是:
 *   - 时间戳(毫秒数)
 *   - ISO 8601 格式字符串
 *   - Date 对象
 *   - null 或 undefined
 * @param {String} format - 格式化类型,可选值:
 *   - 'relative': 相对时间格式 (30分钟前、8小时前、3-24、2023-11-11)
 *   - 'full': 完整日期时间格式 (2023-12-26 14:30)
 *   - 'date': 仅日期格式 (2023-12-26)
 *   - 'time': 仅时间格式 (14:30)
 *   - 'smart': 智能格式 (今天 15:00、昨天 08:30、2023年12月18日 04:46)
 *   - 'short': 短格式 (12-26 14:30)
 *   - 'relative-full': 相对完整时间格式 (刚刚、1分钟前、2小时前、2023-12-26 14:30)
 * @returns {String} 格式化后的日期字符串,如果日期无效则返回 '未知时间'
 *
 * @example
 * // 相对时间
 * formatTime(Date.now() - 1000 * 60 * 30, 'relative'); // "30分钟前"
 * formatTime(Date.now() - 1000 * 60 * 60 * 5, 'relative'); // "5小时前"
 * formatTime('2023-11-11', 'relative'); // "11-11" 或 "2023-11-11"
 *
 * @example
 * // 完整格式
 * formatTime('2023-12-26 14:30:00', 'full'); // "2023-12-26 14:30"
 *
 * @example
 * // 智能格式
 * formatTime(new Date(), 'smart'); // "今天 15:30"
 * formatTime(Date.now() - 24 * 60 * 60 * 1000, 'smart'); // "昨天 15:30"
 *
 * @example
 * // 短格式
 * formatTime('2023-12-26 14:30', 'short'); // "12-26 14:30"
 *
 * @example
 * // 处理无效值
 * formatTime(null, 'full'); // "未知时间"
 * formatTime(undefined, 'smart'); // "未知时间"
 * formatTime('invalid-date', 'relative'); // "未知时间"
 */
export function formatTime(dateTime, format = 'relative') {
    // 处理边界情况
    if (dateTime === null || dateTime === undefined || dateTime === '') {
        return '未知时间';
    }

    // 转换为 Date 对象
    const inputDate = new Date(dateTime);

    // 验证日期有效性
    if (isNaN(inputDate.getTime())) {
        return '未知时间';
    }

    const now = new Date();
    const timeDiff = now.getTime() - inputDate.getTime();

    // 辅助函数:格式化日期部分
    const formatDateParts = (date, includeYear = true) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return includeYear ? { year, month, day } : { month, day };
    };

    // 辅助函数:格式化时间部分
    const formatTimeParts = (date) => {
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return { hours, minutes };
    };

    // 辅助函数:判断是否是今天
    const isToday = (date) => {
        return date.getFullYear() === now.getFullYear() &&
               date.getMonth() === now.getMonth() &&
               date.getDate() === now.getDate();
    };

    // 辅助函数:判断是否是昨天
    const isYesterday = (date) => {
        const yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        return date.getFullYear() === yesterday.getFullYear() &&
               date.getMonth() === yesterday.getMonth() &&
               date.getDate() === yesterday.getDate();
    };

    // 根据格式类型进行处理
    switch (format) {
        case 'relative': {
            // 相对时间格式 (handleDate 原逻辑)
            if (timeDiff < 60 * 60 * 1000) {
                // 1小时内
                const minutes = Math.floor(timeDiff / 1000 / 60);
                return `${minutes}分钟前`;
            } else if (timeDiff < 24 * 60 * 60 * 1000) {
                // 24小时内
                const hours = Math.floor(timeDiff / 1000 / 60 / 60);
                return `${hours}小时前`;
            } else {
                // 超过24小时,显示日期
                const currentYear = now.getFullYear();
                const inputYear = inputDate.getFullYear();
                const month = inputDate.getMonth() + 1;
                const day = inputDate.getDate();

                if (inputYear < currentYear) {
                    return `${inputYear}-${month}-${day}`;
                } else {
                    return `${month}-${day}`;
                }
            }
        }

        case 'full': {
            // 完整日期时间格式 YYYY-MM-DD HH:mm
            const { year, month, day } = formatDateParts(inputDate);
            const { hours, minutes } = formatTimeParts(inputDate);
            return `${year}-${month}-${day} ${hours}:${minutes}`;
        }

        case 'date': {
            // 仅日期格式 YYYY-MM-DD
            const { year, month, day } = formatDateParts(inputDate);
            return `${year}-${month}-${day}`;
        }

        case 'time': {
            // 仅时间格式 HH:mm
            const { hours, minutes } = formatTimeParts(inputDate);
            return `${hours}:${minutes}`;
        }

        case 'smart': {
            // 智能格式 (handleDateTime 原逻辑)
            const { hours, minutes } = formatTimeParts(inputDate);

            if (isToday(inputDate)) {
                return `今天 ${hours}:${minutes}`;
            } else if (isYesterday(inputDate)) {
                return `昨天 ${hours}:${minutes}`;
            } else {
                const { year, month, day } = formatDateParts(inputDate);
                return `${year}年${month}月${day}日 ${hours}:${minutes}`;
            }
        }

        case 'short': {
            // 短格式 MM-DD HH:mm (handleDateTime2 原逻辑)
            const { month, day } = formatDateParts(inputDate, false);
            const { hours, minutes } = formatTimeParts(inputDate);
            return `${month}-${day} ${hours}:${minutes}`;
        }

        case 'relative-full': {
            // 相对完整格式 (handleDateTime3 原逻辑)
            if (timeDiff < 30 * 1000) {
                // 30秒内
                return '刚刚';
            } else if (timeDiff < 60 * 1000) {
                // 1分钟内
                return '1分钟前';
            } else if (timeDiff < 60 * 60 * 1000) {
                // 1小时内
                const minutes = Math.floor(timeDiff / 1000 / 60);
                return `${minutes}分钟前`;
            } else if (timeDiff < 24 * 60 * 60 * 1000) {
                // 24小时内
                const hours = Math.floor(timeDiff / 1000 / 60 / 60);
                return `${hours}小时前`;
            } else {
                // 超过24小时,显示完整日期时间
                const { year, month, day } = formatDateParts(inputDate);
                const { hours, minutes } = formatTimeParts(inputDate);
                return `${year}-${month}-${day} ${hours}:${minutes}`;
            }
        }

        default: {
            // 默认使用相对时间格式
            return formatTime(dateTime, 'relative');
        }
    }
}


/**
 * 处理日期，截取(年)月日或者24小时内或者60分钟内
 * @deprecated 建议使用 formatTime(dateTime, 'relative') 代替
 * @param {Number|String|Date} dateTime 传入的日期时间，可以是数字、字符串或日期对象
 * @returns {String} 处理后的日期字符串 2023-11-11 / 3-24 / 8小时前 / 30分钟前
 *
 * @example
 * handleDate(Date.now() - 1000 * 60 * 30); // "30分钟前"
 * handleDate('2023-11-11'); // "11-11" 或 "2023-11-11"
 */
export function handleDate(dateTime) {
    return formatTime(dateTime, 'relative');
}


/**
 * 处理日期，格式化为 年月日时分/今天时分/昨天时分
 * @deprecated 建议使用 formatTime(dateTime, 'smart') 代替
 * @param {Number|String|Date} dateTime 传入的日期时间，可以是数字、字符串或日期对象
 * @returns {String} 处理后的日期字符串 2023年12月18日 04:46 / 今天 15:00 / 昨天 00:00
 *
 * @example
 * handleDateTime(new Date()); // "今天 15:30"
 * handleDateTime(Date.now() - 24 * 60 * 60 * 1000); // "昨天 15:30"
 * handleDateTime('2023-12-18 04:46'); // "2023年12月18日 04:46"
 */
export function handleDateTime(dateTime) {
    return formatTime(dateTime, 'smart');
}


/**
 * 处理日期，格式化为 月日时分
 * @deprecated 建议使用 formatTime(dateTime, 'short') 代替
 * @param {Number|String|Date} dateTime 传入的日期时间，可以是数字、字符串或日期对象
 * @returns {String} 处理后的日期字符串 12-26 02:53
 *
 * @example
 * handleDateTime2('2023-12-26 02:53'); // "12-26 02:53"
 */
export function handleDateTime2(dateTime) {
    return formatTime(dateTime, 'short');
}


/**
 * 处理时间，格式化为 年-月-日 时:分 或者 n小时前 或者 n分钟前
 * @deprecated 建议使用 formatTime(dateTime, 'relative-full') 代替
 * @param {Number|String|Date} dateTime 传入的日期时间，可以是数字、字符串或日期对象
 * @returns {String} YYYY-MM-DD HH:mm / n小时前 / n分钟前 / 刚刚
 *
 * @example
 * handleDateTime3(Date.now() - 1000 * 20); // "刚刚"
 * handleDateTime3(Date.now() - 1000 * 60 * 5); // "5分钟前"
 * handleDateTime3('2023-12-26 14:30'); // "2023-12-26 14:30"
 */
export function handleDateTime3(dateTime) {
    return formatTime(dateTime, 'relative-full');
}


/**
 * 计算昵称长度，中日文字符占2长度，其他1长度，用于判断昵称长度是否超出32的限制
 * @param {String} nickname 用户输入的昵称名
 * @returns {Number} 昵称的官方长度
 */
export function getNicknameLength(nickname) {
    let length = 0;
    for (let i = 0; i < nickname.length; i++) {
        // 使用正则表达式检测字符是否为中文或日文
        if (/[\u4e00-\u9fa5\u0800-\u4e00]/.test(nickname[i])) {
            length += 2;
        } else {
            length += 1;
        }
    }
    return length;
}


/**
 * 根据经验值计算用户等级
 * @param {Number} exp 经验值
 * @returns {Number} 等级
 */
export function handleLevel(exp) {
    if (exp < 50) {
        return 0;
    } else if (exp < 200) {
        return 1;
    } else if (exp < 1500) {
        return 2;
    } else if (exp < 4500) {
        return 3;
    } else if (exp < 10800) {
        return 4;
    } else if (exp < 28800) {
        return 5;
    } else {
        return 6;
    }
}


/**
 * 生成随机uuid
 * @returns {String} 随机的uuid 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
 */
export function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (Math.random() * 16) | 0,
            v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

/**
 * 格式化硬币显示（保留一位小数，避免浮点数精度问题）
 * @param {Number} coins 硬币数量
 * @returns {String} 格式化后的硬币数量，例如 '7.3'
 */
export function formatCoins(coins) {
    if (coins === null || coins === undefined) {
        return '0.0';
    }
    return Number(coins).toFixed(1);
}