var defaultTarget = 'http://localhost:8080/';

module.exports = [
{
   context: ['/api'],
   target: defaultTarget,
   pathRewrite: {'^/api' : ''}, 
   secure: false
}
];
